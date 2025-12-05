/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// Configuração Global (opcional, define a região padrão)
setGlobalOptions({ region: "us-central1" });

// --- CONFIGURAÇÃO DE NÍVEIS ---
const NIVEIS = {
    INICIANTE: 'Iniciante',       // 0 - 499
    INTERMEDIARIO: 'Intermediário', // 500 - 1999
    AVANCADO: 'Avançado',         // 2000 - 4999
    ESPECIALISTA: 'Especialista'  // 5000+
};

// Função auxiliar (Lógica pura)
const calcularNovoNivel = (pontosAtuais) => {
    if (pontosAtuais >= 5000) return NIVEIS.ESPECIALISTA;
    if (pontosAtuais >= 2000) return NIVEIS.AVANCADO;
    if (pontosAtuais >= 500) return NIVEIS.INTERMEDIARIO;
    return NIVEIS.INICIANTE;
};

// --- GATILHO 1: ALGUÉM ENVIOU UMA SOLICITAÇÃO (SINTAXE V2) ---
exports.onNovaSolicitacao = onDocumentCreated("solicitacoes/{solicitacaoId}", async (event) => {
    // Na V2, 'event.data' é o snapshot do documento. Usamos .data() nele para pegar o JSON.
    const snapshot = event.data;
    if (!snapshot) {
        return; // Nenhum dado associado ao evento
    }

    const dados = snapshot.data();
    const remetenteId = dados.remetenteUserId;
    const destinatarioId = dados.destinatarioUserId;

    const batch = db.batch();
    const userRefRemetente = db.collection('users').doc(remetenteId);
    const userRefDestinatario = db.collection('users').doc(destinatarioId);

    // 1. DAR PONTOS PARA QUEM ENVIOU
    const docRemetente = await userRefRemetente.get();
    if (docRemetente.exists) {
        const gamif = docRemetente.data().gamification || {};
        const pontosAtuais = gamif.points || 0;
        const novosPontos = pontosAtuais + 10;

        const novoNivel = calcularNovoNivel(novosPontos);

        batch.update(userRefRemetente, {
            'gamification.points': novosPontos,
            'gamification.level': novoNivel
        });
    }

    // 2. REGISTRAR INCIDENTE PARA QUEM RECEBEU
    batch.update(userRefDestinatario, {
        'gamification.lastIncidentDate': admin.firestore.FieldValue.serverTimestamp()
    });

    await batch.commit();
    console.log(`Processado V2: +10 pts para ${remetenteId}, Incidente para ${destinatarioId}`);
});

// --- GATILHO 2: BÔNUS DIÁRIO (SINTAXE V2) ---
exports.verificarBonusDiario = onSchedule({
    schedule: "every 24 hours",
    timeZone: "America/Sao_Paulo",
}, async (event) => {
    const now = admin.firestore.Timestamp.now();
    const todayDate = now.toDate().setHours(0,0,0,0);

    const usersSnapshot = await db.collection('users').get();
    const batch = db.batch();
    let batchCount = 0;

    usersSnapshot.forEach(doc => {
        const data = doc.data();
        const gamif = data.gamification || {};

        if (!gamif.lastIncidentDate) return;

        const lastIncident = gamif.lastIncidentDate.toDate();
        const lastBonusDate = gamif.lastBonusDate ? gamif.lastBonusDate.toDate().setHours(0,0,0,0) : 0;

        const diffTime = Math.abs(now.toDate() - lastIncident);
        const daysClean = Math.floor(diffTime / (1000 * 60 * 60 * 24));

        if (lastBonusDate === todayDate) return; // Já ganhou hoje

        let pontosBonus = 0;

        if (daysClean > 0) {
            if (daysClean % 30 === 0) pontosBonus = 150;
            else if (daysClean === 15) pontosBonus = 60;
            else if (daysClean === 7) pontosBonus = 30;
        }

        if (pontosBonus > 0) {
            const currentPoints = gamif.points || 0;
            const newTotal = currentPoints + pontosBonus;
            const newLevel = calcularNovoNivel(newTotal);

            batch.update(doc.ref, {
                'gamification.points': newTotal,
                'gamification.level': newLevel,
                'gamification.lastBonusDate': now
            });
            batchCount++;
        }
    });

    if (batchCount > 0) await batch.commit();
    console.log(`Bônus diário rodou. ${batchCount} usuários premiados.`);
});