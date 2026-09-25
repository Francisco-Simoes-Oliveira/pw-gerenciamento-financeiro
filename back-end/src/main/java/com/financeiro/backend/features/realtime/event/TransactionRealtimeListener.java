package com.financeiro.backend.features.realtime.event;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.financeiro.backend.features.realtime.dto.TransactionRealtimeMessage;
import com.financeiro.backend.features.realtime.websocket.WalletRealtimeWebSocketHandler;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;
import com.financeiro.backend.features.wallet.repository.WalletRepository;

@Component
public class TransactionRealtimeListener {

    private final WalletRepository walletRepository;
    private final WalletMemberRepository memberRepository;
    private final WalletRealtimeWebSocketHandler webSocketHandler;

    public TransactionRealtimeListener(
            WalletRepository walletRepository,
            WalletMemberRepository memberRepository,
            WalletRealtimeWebSocketHandler webSocketHandler
    ) {
        this.walletRepository = walletRepository;
        this.memberRepository = memberRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionChanged(TransactionChangedEvent event) {
        Set<UUID> recipients = resolveRecipients(event.walletIds());
        if (recipients.isEmpty()) {
            return;
        }

        TransactionRealtimeMessage message = new TransactionRealtimeMessage(
                "TRANSACTION_" + event.type().name(),
                event.transactionId(),
                event.walletIds(),
                event.changedByUserId(),
                event.occurredAt()
        );

        webSocketHandler.sendToUsers(recipients, message);
    }

    private Set<UUID> resolveRecipients(Set<UUID> walletIds) {
        Set<UUID> recipients = new HashSet<>();

        for (UUID walletId : walletIds) {
            Wallet wallet = walletRepository.findById(walletId).orElse(null);
            if (wallet == null) {
                continue;
            }

            if (wallet.getOwner() != null && wallet.getOwner().getId() != null) {
                recipients.add(wallet.getOwner().getId());
            }

            memberRepository.findByWalletId(walletId).forEach(member -> {
                if (member.getUser() != null && member.getUser().getId() != null) {
                    recipients.add(member.getUser().getId());
                }
            });
        }

        return recipients;
    }
}
