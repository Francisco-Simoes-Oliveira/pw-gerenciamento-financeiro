package com.financeiro.backend.features.realtime.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.realtime.dto.TransactionRealtimeMessage;
import com.financeiro.backend.features.realtime.websocket.WalletRealtimeWebSocketHandler;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.entity.WalletMember;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;
import com.financeiro.backend.features.wallet.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class TransactionRealtimeListenerTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletMemberRepository memberRepository;

    @Mock
    private WalletRealtimeWebSocketHandler webSocketHandler;

    @InjectMocks
    private TransactionRealtimeListener listener;

    @Test
    void shouldNotifyOwnerAndMembersOfAffectedWallet() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        User owner = new User();
        owner.setId(ownerId);
        User memberUser = new User();
        memberUser.setId(memberId);

        Wallet wallet = Wallet.builder().id(walletId).owner(owner).build();
        WalletMember member = WalletMember.builder().wallet(wallet).user(memberUser).build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(memberRepository.findByWalletId(walletId)).thenReturn(List.of(member));

        TransactionChangedEvent event = new TransactionChangedEvent(
                TransactionChangedEvent.ChangeType.CREATED,
                transactionId,
                Set.of(walletId),
                ownerId,
                LocalDateTime.now()
        );

        listener.onTransactionChanged(event);

        ArgumentCaptor<TransactionRealtimeMessage> messageCaptor =
                ArgumentCaptor.forClass(TransactionRealtimeMessage.class);
        verify(webSocketHandler).sendToUsers(eq(Set.of(ownerId, memberId)), messageCaptor.capture());

        TransactionRealtimeMessage message = messageCaptor.getValue();
        assertEquals("TRANSACTION_CREATED", message.type());
        assertEquals(transactionId, message.transactionId());
        assertEquals(Set.of(walletId), message.walletIds());
    }
}
