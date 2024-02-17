package com.taha.javabankapplication.service.impl;

import com.taha.javabankapplication.dto.*;
import com.taha.javabankapplication.entity.User;
import com.taha.javabankapplication.repository.UserRepository;
import com.taha.javabankapplication.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    TransactionService transactionService;


    public BankResponse createAccount(UserRequest userRequest) {
        /**
         * Creating an account - saving a new user into the db
         * check if user already has an account
         */
        if (userRepository.existsByEmail(userRequest.getEmail())){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .surName(userRequest.getSurName())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .build();

        User savedUser = userRepository.save(newUser);
        //Send email Alert
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody("Congratulations! Your Account Has been Successfully Created.\nYour Account Details: \n" +
                        "Account Name: " + savedUser.getFirstName() + " " + savedUser.getSurName() + " " + savedUser.getOtherName() + "\nAccount Number: " + savedUser.getAccountNumber())
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(savedUser.getFirstName() + " " + savedUser.getSurName() + " " + savedUser.getOtherName())
                        .build())
                .build();

    }

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest enquiryRequest) {
        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if(!isAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_SUCCESS)
                .accountInfo(AccountInfo.builder()
                     .accountBalance(foundUser.getAccountBalance())
                     .accountNumber(enquiryRequest.getAccountNumber())
                     .accountName(foundUser.getFirstName() + " " + foundUser.getOtherName() + " " + foundUser.getSurName())
                .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest) {
        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if(!isAccountExist){
            return AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE;
        }
        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return foundUser.getFirstName() + " " + foundUser.getOtherName() + " " + foundUser.getSurName();
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest creditDebitRequest) {
        boolean isAccountExist = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if(!isAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToCredit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(creditDebitRequest.getAmount()));
        userRepository.save(userToCredit);

        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(userToCredit.getAccountNumber())
                .transactionType("CREDIT")
                .amount(creditDebitRequest.getAmount())
                .build();
        transactionService.saveTransaction(transactionDto);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(userToCredit.getFirstName() + " " + userToCredit.getOtherName() + " " + userToCredit.getSurName())
                        .accountBalance(userToCredit.getAccountBalance())
                        .accountNumber(userToCredit.getAccountNumber())
                .build())
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if(!isAccountExist){
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User userToDebit = userRepository.findByAccountNumber(request.getAccountNumber());
        if(userToDebit.getAccountBalance().compareTo(request.getAmount()) < 0){
           return BankResponse.builder()
                   .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                   .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                   .accountInfo(null)
                   .build();
        }

        else {
            userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(request.getAmount()));
            userRepository.save(userToDebit);

            TransactionDto transactionDto = TransactionDto.builder()
                    .accountNumber(userToDebit.getAccountNumber())
                    .transactionType("DEBIT")
                    .amount(request.getAmount())
                    .build();
            transactionService.saveTransaction(transactionDto);

            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS)
                    .responseMessage(AccountUtils.ACCOUNT_DEBITED_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountName(userToDebit.getFirstName() + " " + userToDebit.getOtherName() + " " + userToDebit.getSurName())
                            .accountBalance(userToDebit.getAccountBalance())
                            .accountNumber(userToDebit.getAccountNumber())
                    .build())
                    .build();
        }
    }

    @Override
    public BankResponse transferAccount(TransferRequest request) {
        boolean isSourceAccountExist = userRepository.existsByAccountNumber(request.getSourceAccountNumber());
        boolean isDestinationAccountExist = userRepository.existsByAccountNumber(request.getDestinationAccountNumber());
          if(!isDestinationAccountExist){
              return BankResponse.builder()
                      .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                      .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                      .accountInfo(null)
                      .build();
          }
          else{
              User sourceUser = userRepository.findByAccountNumber(request.getSourceAccountNumber());
              if(sourceUser.getAccountBalance().compareTo(request.getAmount()) < 0) {
                  return BankResponse.builder()
                          .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                          .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                          .accountInfo(null)
                          .build();
              }
              sourceUser.setAccountBalance(sourceUser.getAccountBalance().subtract(request.getAmount()));
              userRepository.save(sourceUser);
              EmailDetails debitAlert = EmailDetails.builder()
                      .subject("DEBIT ALERT")
                      .recipient(sourceUser.getEmail())
                      .messageBody("The sum of " + request.getAmount() + " has been deducted from your account! Your current balance is: " + sourceUser.getAccountBalance())
                      .build();

              emailService.sendEmailAlert(debitAlert);

              User destinationUser = userRepository.findByAccountNumber(request.getDestinationAccountNumber());
              destinationUser.setAccountBalance(destinationUser.getAccountBalance().add(request.getAmount()));
              userRepository.save(destinationUser);

              EmailDetails creditAlert = EmailDetails.builder()
                      .subject("TRANSFER ALERT")
                      .recipient(destinationUser.getEmail())
                      .messageBody("The sum of " + request.getAmount() + " has been sent to your account from: " + sourceUser.getFirstName() + " "
                      + sourceUser.getOtherName() + " "
                      + sourceUser.getSurName() + " And your current balance is: " + destinationUser.getAccountBalance())
                      .build();
              emailService.sendEmailAlert(creditAlert);

              TransactionDto transactionDto = TransactionDto.builder()
                      .accountNumber(destinationUser.getAccountNumber())
                      .transactionType("TRANSFER")
                      .amount(request.getAmount())
                      .build();
              transactionService.saveTransaction(transactionDto);

              return BankResponse.builder()
                      .responseCode(AccountUtils.TRANSFER_SUCCESS_CODE)
                      .responseMessage(AccountUtils.TRANSFER_SUCCESS_MESSAGE)
                      .accountInfo(null)
                      .build();
          }
    }

}
