package com.securitymak.securitymak.specification;

import com.securitymak.securitymak.model.Case;
import com.securitymak.securitymak.model.CaseStatus;
import com.securitymak.securitymak.model.SensitivityLevel;
import org.springframework.data.jpa.domain.Specification;

public class CaseSpecification {

    public static Specification<Case> belongsToTenant(Long tenantId) {
        return (root, query, cb) ->
                cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<Case> titleContains(String title) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                );
    }

    public static Specification<Case> hasStatus(CaseStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Case> hasSensitivity(SensitivityLevel level) {
        return (root, query, cb) ->
                cb.equal(root.get("sensitivityLevel"), level);
    }

    public static Specification<Case> sensitivityLessThanEqual(SensitivityLevel level) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("sensitivityLevel"), level);
    }
}
