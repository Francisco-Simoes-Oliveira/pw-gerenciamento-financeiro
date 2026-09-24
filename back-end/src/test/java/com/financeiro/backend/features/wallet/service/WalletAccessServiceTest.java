package com.financeiro.backend.features.wallet.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.entity.WalletMember;
import com.financeiro.backend.features.wallet.enums.WalletPermission;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;

@ExtendWith(MockitoExtension.class)
class WalletAccessServiceTest {

    @Mock
    private WalletMemberRepository memberRepository;

    private WalletAccessService accessService;
    private User owner;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        accessService = new WalletAccessService(memberRepository);
        owner = new User();
        owner.setId(UUID.randomUUID());
        wallet = Wallet.builder().id(UUID.randomUUID()).owner(owner).build();
    }

    @Test
    void ownerCanViewAndEditTransactionsAndManageWallet() {
        assertDoesNotThrow(() -> accessService.requireView(wallet, owner.getId()));
        assertDoesNotThrow(() -> accessService.requireTransactionEdit(wallet, owner.getId()));
        assertDoesNotThrow(() -> accessService.requireOwner(wallet, owner.getId()));
    }

    @Test
    void editorCanViewAndEditTransactionsButIsNotOwner() {
        UUID editorId = UUID.randomUUID();
        User editorUser = new User();
        editorUser.setId(editorId);
        WalletMember editor = WalletMember.builder()
                .wallet(wallet)
                .user(editorUser)
                .permission(WalletPermission.EDITOR)
                .build();

        when(memberRepository.existsByWalletIdAndUserId(wallet.getId(), editorId)).thenReturn(true);
        when(memberRepository.findByWalletIdAndUserId(wallet.getId(), editorId)).thenReturn(Optional.of(editor));

        assertDoesNotThrow(() -> accessService.requireView(wallet, editorId));
        assertDoesNotThrow(() -> accessService.requireTransactionEdit(wallet, editorId));
        assertThrows(AccessDeniedException.class, () -> accessService.requireOwner(wallet, editorId));
    }

    @Test
    void viewerCanViewButCannotEditTransactions() {
        UUID viewerId = UUID.randomUUID();
        User viewerUser = new User();
        viewerUser.setId(viewerId);
        WalletMember viewer = WalletMember.builder()
                .wallet(wallet)
                .user(viewerUser)
                .permission(WalletPermission.VIEWER)
                .build();

        when(memberRepository.existsByWalletIdAndUserId(wallet.getId(), viewerId)).thenReturn(true);
        when(memberRepository.findByWalletIdAndUserId(wallet.getId(), viewerId)).thenReturn(Optional.of(viewer));

        assertDoesNotThrow(() -> accessService.requireView(wallet, viewerId));
        assertThrows(AccessDeniedException.class, () -> accessService.requireTransactionEdit(wallet, viewerId));
    }

    @Test
    void nonMemberCannotView() {
        UUID outsiderId = UUID.randomUUID();
        when(memberRepository.existsByWalletIdAndUserId(wallet.getId(), outsiderId)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> accessService.requireView(wallet, outsiderId));
    }
}
