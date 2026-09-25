package com.financeiro.backend.features.reports.specification;

import org.springframework.data.jpa.domain.Specification;

import com.financeiro.backend.features.reports.dto.request.ReportFilter;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.wallet.entity.WalletMember;

public class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> withFilter(ReportFilter filter, java.util.UUID userId) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            var membership = query.subquery(Long.class);
            var memberRoot = membership.from(WalletMember.class);
            membership.select(cb.literal(1L));
            membership.where(
                    cb.equal(memberRoot.get("wallet").get("id"), root.get("wallet").get("id")),
                    cb.equal(memberRoot.get("user").get("id"), userId)
            );

            var canAccessWallet = cb.or(
                    cb.equal(root.get("wallet").get("owner").get("id"), userId),
                    cb.exists(membership)
            );
            predicates = cb.and(predicates, canAccessWallet);

            if (filter.getWalletId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("wallet").get("id"), filter.getWalletId()));
            }
            if (filter.getCategoryId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }
            if (filter.getStartDate() != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate().atStartOfDay()));
            }
            if (filter.getEndDate() != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate().atTime(23, 59, 59)));
            }
            if (filter.getType() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("type"), filter.getType()));
            }
            if (filter.getStatus() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), filter.getStatus()));
            }
            return predicates;
        };
    }
}
