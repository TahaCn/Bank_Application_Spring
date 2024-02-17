package com.taha.javabankapplication.repository;

import com.taha.javabankapplication.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {


}
