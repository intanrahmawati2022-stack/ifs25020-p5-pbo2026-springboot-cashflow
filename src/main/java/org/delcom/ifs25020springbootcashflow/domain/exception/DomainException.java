package org.delcom.ifs25020springbootcashflow.domain.exception;

/** Checked exception dasar untuk semua kegagalan aturan domain. */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }
}
