package com.financeiro.backend.features.wallet.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.financeiro.backend.common.exception.ConflictException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.subscription.repository.UserSubscriptionRepository;
import com.financeiro.backend.features.transaction.repository.TransactionRepository;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletRequest;
import com.financeiro.backend.features.wallet.dto.response.WalletResponse;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.entity.WalletMember;
import com.financeiro.backend.features.wallet.enums.WalletPermission;
import com.financeiro.backend.features.wallet.mapper.WalletMapper;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository repository;

    @Mock
    private WalletMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private WalletMapper mapper;

    @Mock
    private WalletAccessService walletAccessService;

    @InjectMocks
    private WalletServiceImpl service;

    private User owner;
    private Wallet ownedWallet;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        ownedWallet = Wallet.builder().id(UUID.randomUUID()).owner(owner).build();
    }

    @Test
    void alterShouldRequireOwnerPermission() {
        UpdateWalletRequest request = new UpdateWalletRequest();
        request.setName("Nova carteira");
        WalletResponse response = new WalletResponse();

        when(repository.findById(ownedWallet.getId())).thenReturn(Optional.of(ownedWallet));
        when(repository.save(ownedWallet)).thenReturn(ownedWallet);
        when(mapper.toResponse(ownedWallet)).thenReturn(response);

        service.alter(ownedWallet.getId(), owner.getId(), request);

        verify(walletAccessService).requireOwner(ownedWallet, owner.getId());
        verify(mapper).updateEntityFromDto(request, ownedWallet);
    }

    @Test
    void addMemberShouldRejectOwnerAsRegularMember() {
        when(repository.findById(ownedWallet.getId())).thenReturn(Optional.of(ownedWallet));

        assertThrows(ConflictException.class,
                () -> service.addMember(
                        ownedWallet.getId(),
                        owner.getId(),
                        owner.getId(),
                        WalletPermission.EDITOR.name()
                ));
    }

    @Test
    void addMemberShouldRejectOwnerRoleForRegularMember() {
        UUID targetUserId = UUID.randomUUID();
        when(repository.findById(ownedWallet.getId())).thenReturn(Optional.of(ownedWallet));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.addMember(
                        ownedWallet.getId(),
                        owner.getId(),
                        targetUserId,
                        WalletPermission.OWNER.name()
                ));

        assertEquals("A permissão OWNER é reservada ao proprietário da carteira.", ex.getMessage());
    }

    @Test
    void listShouldMergeOwnedAndSharedWalletsWithoutDuplicates() {
        User memberUser = new User();
        memberUser.setId(owner.getId());
        Wallet sharedWallet = Wallet.builder().id(UUID.randomUUID()).owner(new User()).build();
        WalletMember membership = WalletMember.builder()
                .wallet(sharedWallet)
                .user(memberUser)
                .permission(WalletPermission.VIEWER)
                .build();

        WalletResponse ownedResponse = new WalletResponse();
        ownedResponse.setId(ownedWallet.getId());
        WalletResponse sharedResponse = new WalletResponse();
        sharedResponse.setId(sharedWallet.getId());

        when(repository.findByOwnerId(owner.getId())).thenReturn(List.of(ownedWallet));
        when(memberRepository.findByUserId(owner.getId())).thenReturn(List.of(membership));
        when(mapper.toResponse(ownedWallet)).thenReturn(ownedResponse);
        when(mapper.toResponse(sharedWallet)).thenReturn(sharedResponse);

        List<WalletResponse> result = service.listByOwner(owner.getId());

        assertEquals(2, result.size());
        assertEquals(ownedWallet.getId(), result.get(0).getId());
        assertEquals(sharedWallet.getId(), result.get(1).getId());
    }
}
