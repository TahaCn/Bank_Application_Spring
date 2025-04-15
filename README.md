# Bank_Application_Spring

A Basic Bank Application built with Java Spring Boot, providing RESTful APIs for user account management, transactions (credit, debit, transfer), balance & name enquiries, and bank statement generation. Integrated with MySQL, Spring Data JPA, JavaMailSender for email alerts, and Lombok for boilerplate reduction.

🛠️ Technologies & Tools
  -Language & Frameworks:
      -Java 17+
      -Spring Boot (Web, Data JPA, Mail)
      -Spring MVC (REST controllers)
  -Persistance:
      -MySQL
      -Spring Data JPA Repositories
  -Email:
      -Spring's JavaMailSender for account creation, debit/credit/transfer alerts.
  -Utilities:
      -Lombok
      -Custom AccountUtils for account-number generation and response codes/messages
  -Dependency Management:
      -Maven

🚀 Features
   -Account Management:
       -Create new bank accounts (POST /api/user)
       -Prevent duplicate accounts by email
       -Email confirmation upon successful creation
   -Enquiries
       -Balance Enquiry: GET /api/user/balanceEnquiry
       -Name Enquiry: GET /api/user/nameEnquiry
   -Transactions
       -Credit: POST /api/user/credit
       -Debit: POST /api/user/debit
       -Transfer: POST /api/user/transfer
       -Records each operation in transactions table 
   -Bank Statement
       -Generate filtered statement by date range:
        GET /bankStatement?accountNumber={acct}&startDate={yyyy-MM-dd}&endDate={yyyy-MM-dd} 
   -Email Alerts
       -Automatic email notifications for account creation, debit, credit, and transfers

📋 Prerequisites
   -Java 17 or higher
   -Maven 3.6+
   -MySQL database (configured in application.properties)
   -A Gmail account (or SMTP‑capable) for email alerts
