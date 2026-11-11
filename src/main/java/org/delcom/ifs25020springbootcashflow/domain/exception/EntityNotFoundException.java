package org.delcom.ifs25020springbootcashflow.domain.exception;

/** Entitas tidak ditemukan berdasarkan ID. */
public class EntityNotFoundException extends DomainException {
    public EntityNotFoundException(String entity, Object id) {
        super(entity + " dengan ID " + id + " tidak ditemukan");
    }
}
