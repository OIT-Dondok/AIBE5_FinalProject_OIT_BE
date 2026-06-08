ALTER TABLE mission_log
    ADD CONSTRAINT chk_ml_status_decision_reason
        CHECK (
            CHAR_LENGTH(caption) BETWEEN 5 AND 100
            AND (
                (
                    certification_status = 'FAILED'
                    AND (
                        (
                            decision_type = 'MANUAL_REJECT'
                            AND failure_reason IS NULL
                            AND reject_reason_code IS NOT NULL
                        )
                        OR
                        (
                            decision_type = 'AUTO_REJECT'
                            AND failure_reason IS NOT NULL
                            AND reject_reason_code IS NULL
                            AND reject_memo IS NULL
                        )
                    )
                )
                OR
                (
                    certification_status <> 'FAILED'
                    AND failure_reason IS NULL
                )
            )
        );
