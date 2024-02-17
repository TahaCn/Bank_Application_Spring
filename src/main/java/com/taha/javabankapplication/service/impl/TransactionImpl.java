package com.taha.javabankapplication.service.impl;

import com.taha.javabankapplication.dto.TransactionDto;
import com.taha.javabankapplication.entity.Transaction;
import com.taha.javabankapplication.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionImpl implements  TransactionService{

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public void saveTransaction(TransactionDto transactionDto) {
      Transaction transaction = Transaction.builder()
              .transactionType(transactionDto.getTransactionType())
              .accountNumber(transactionDto.getAccountNumber())
              .amount(transactionDto.getAmount())
              .status("SUCCESS")
              .build();
      transactionRepository.save(transaction);
      System.out.println("Transaction saved successfully!");
    }
}
