package ma.enset.ebankingbackend;

import ma.enset.ebankingbackend.entities.AccountOperation;
import ma.enset.ebankingbackend.entities.CurrentAccount;
import ma.enset.ebankingbackend.entities.Customer;
import ma.enset.ebankingbackend.entities.SavingAccount;
import ma.enset.ebankingbackend.enums.AccountStatus;
import ma.enset.ebankingbackend.enums.OperationType;
import ma.enset.ebankingbackend.repositories.AccountOperationRepository;
import ma.enset.ebankingbackend.repositories.BankAccountRepository;
import ma.enset.ebankingbackend.repositories.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankingBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository){
        return args ->{
            Stream.of("Ali","Ahmed","Morad").forEach(name ->{
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name+"@gmail.com");
                customerRepository.save(customer);
            });
            customerRepository.findAll().forEach(customer -> {
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setBalance((int)(Math.random()*9000));
                currentAccount.setCreatedAt(new Date());
                currentAccount.setCustomer(customer);
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setOverDraft(900);
                currentAccount.setId(UUID.randomUUID().toString());
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setBalance((int)(Math.random()*9000));
                savingAccount.setCreatedAt(new Date());
                savingAccount.setCustomer(customer);
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setIntersetRave(5);
                savingAccount.setId(UUID.randomUUID().toString());
                bankAccountRepository.save(savingAccount);
            });
            bankAccountRepository.findAll().forEach(acc->{
                for (int i = 0; i < 5; i++) {
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setBankAccount(acc);
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setType(Math.random()>0.5? OperationType.CREDIT:OperationType.DEBIT);
                    accountOperation.setAmount((int)(Math.random()*12000));
                    accountOperationRepository.save(accountOperation);
                }
            });
        };
    }

}
