package ma.enset.ebankingbackend;

import ma.enset.ebankingbackend.dtos.BankAccountDto;
import ma.enset.ebankingbackend.dtos.CurrentBankAccountDto;
import ma.enset.ebankingbackend.dtos.CustomerDto;
import ma.enset.ebankingbackend.dtos.SavingBankAccountDto;
import ma.enset.ebankingbackend.entities.AccountOperation;
import ma.enset.ebankingbackend.entities.CurrentAccount;
import ma.enset.ebankingbackend.entities.Customer;
import ma.enset.ebankingbackend.entities.SavingAccount;
import ma.enset.ebankingbackend.enums.AccountStatus;
import ma.enset.ebankingbackend.enums.OperationType;
import ma.enset.ebankingbackend.exceptions.CustomerNotFoundExcep;
import ma.enset.ebankingbackend.repositories.AccountOperationRepository;
import ma.enset.ebankingbackend.repositories.BankAccountRepository;
import ma.enset.ebankingbackend.repositories.CustomerRepository;
import ma.enset.ebankingbackend.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankingBackendApplication.class, args);
    }



    @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService){
        return args -> {
            Stream.of("Ali","Ahmed","Younes").forEach(name->{
                CustomerDto costumer=new CustomerDto();
                costumer.setName(name);
                costumer.setEmail(name+"@gmail.com");
                bankAccountService.saveCustomer(costumer);
            });

            /*bankAccountService.listCustomer().forEach(costumer -> {
                try {
                    bankAccountService.saveCurrentAccount(Math.random() * 9000, 9000, costumer.getId());
                    bankAccountService.saveSavingAccount(Math.random() * 12000, 5.5, costumer.getId());
                    List<BankAccountDto> bankAccounts=bankAccountService.bankAccountList();
                    for (BankAccountDto bankAccount:bankAccounts){
                        for (int i=0;i<10;i++){
                            String accountId;
                            if (bankAccount instanceof CurrentBankAccountDto){
                                accountId=((CurrentBankAccountDto) bankAccount).getId();
                            }else {
                                accountId=((SavingBankAccountDto)bankAccount).getId();
                            }
                            bankAccountService.credit(accountId,1000+Math.random()*120000,"credit");
                            bankAccountService.debit(accountId,1000+Math.random()*9000,"debit");
                        }
                    }
                }catch (CustomerNotFoundExcep excep){
                    excep.printStackTrace();
                } catch (BankAccountNotFoundExcept e) {
                    e.printStackTrace();
                } catch (BalanceNotSufficientExcep e) {
                    e.printStackTrace();
                }
            });*/

            bankAccountService.listCustomer().forEach(customer->{
                try {
                    bankAccountService.saveCurrentAccount(Math.random()*90000,9000,customer.getId());
                    bankAccountService.saveSavingAccount(Math.random()*120000,5.5,customer.getId());

                } catch (CustomerNotFoundExcep e) {
                    e.printStackTrace();
                }
            });
            List<BankAccountDto> bankAccounts = bankAccountService.bankAccountList();
            for (BankAccountDto bankAccount:bankAccounts){
                for (int i = 0; i <10 ; i++) {
                    String accountId;
                    if(bankAccount instanceof SavingBankAccountDto){
                        accountId=((SavingBankAccountDto) bankAccount).getId();
                    } else{
                        accountId=((CurrentBankAccountDto) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId,10000+Math.random()*120000,"Credit");
                    bankAccountService.debit(accountId,1000+Math.random()*9000,"Debit");
                }
            }
        };
    }

    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository){
        return args ->{
            Stream.of("Ali","Ahmed","Younes").forEach(name ->{
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
                savingAccount.setInterestRave(5);
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
