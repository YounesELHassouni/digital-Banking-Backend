package ma.enset.ebankingbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.ebankingbackend.dtos.*;
import ma.enset.ebankingbackend.entities.*;
import ma.enset.ebankingbackend.enums.OperationType;
import ma.enset.ebankingbackend.exceptions.BalanceNotSufficientExcep;
import ma.enset.ebankingbackend.exceptions.BankAccountNotFoundExcept;
import ma.enset.ebankingbackend.exceptions.CustomerNotFoundExcep;
import ma.enset.ebankingbackend.mappers.BankAccountMapperImpl;
import ma.enset.ebankingbackend.repositories.AccountOperationRepository;
import ma.enset.ebankingbackend.repositories.BankAccountRepository;
import ma.enset.ebankingbackend.repositories.CustomerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService{

    private CustomerRepository customerRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountRepository bankAccountRepository;
    private BankAccountMapperImpl dtoMapper;

    @Override
    public  CustomerDto saveCustomer(CustomerDto customerDto) {
        log.info("saving new customer");
        Customer customer=dtoMapper.fromCustomerDto(customerDto);
        Customer savedCustomer=customerRepository.save(customer);
        return dtoMapper.formCustomer(savedCustomer);
    }

    @Override
    public CurrentBankAccountDto saveCurrentAccount(double amount, double overDraft, Long customerId) throws CustomerNotFoundExcep {
            Customer customer=customerRepository.findById(customerId).orElse(null);
            if (customer==null)
                throw new CustomerNotFoundExcep("customer not found");
            CurrentAccount currentAccount=new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(amount);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        CurrentAccount savedCurrentAccount=bankAccountRepository.save(currentAccount);
            return dtoMapper.fromCurrentBankAccount(savedCurrentAccount);
    }

    @Override
    public SavingBankAccountDto saveSavingAccount(double amount, double interestRate, Long customerId) {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if (customer==null)
            throw new RuntimeException("customer not found");
        SavingAccount savingAccount=new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(amount);
        savingAccount.setInterestRave(interestRate);
        savingAccount.setCustomer(customer);
        SavingAccount savedSavingAccount=bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedSavingAccount);
    }

    public List<CustomerDto> listCustomer() {
        List<Customer> customers=customerRepository.findAll();
        List<CustomerDto> customerDtos= customers.stream()
                .map(customer->dtoMapper.formCustomer(customer))
                .collect(Collectors.toList());
        return customerDtos ;
    }

    @Override
    public BankAccountDto getBankAccount(String AccountId) throws BankAccountNotFoundExcept {
        BankAccount bankAccount = bankAccountRepository.findById(AccountId).orElseThrow(() -> new BankAccountNotFoundExcept(" Account not found!"));
        if (bankAccount instanceof SavingAccount) {
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);
        } else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        }
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BalanceNotSufficientExcep, BankAccountNotFoundExcept {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundExcept(" Account not found!"));
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BalanceNotSufficientExcep, BankAccountNotFoundExcept {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundExcept(" Account not found!"));
        if(bankAccount.getBalance()<amount){
            throw new BalanceNotSufficientExcep("balance Not sufficient !");
        }
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()-amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(double tranferAmount, String idAccountSource, String idAccountDest) throws BankAccountNotFoundExcept,BalanceNotSufficientExcep{
        debit(idAccountSource,tranferAmount,"transfer to "+idAccountDest);
        credit(idAccountDest,tranferAmount,"transfer from "+idAccountSource);
    }
    @Override
    public List<BankAccountDto> bankAccountList(){
        List<BankAccount> bankAccounts= bankAccountRepository.findAll();
        List<BankAccountDto> bankAccountDtos=bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof CurrentAccount){
                CurrentAccount currentAccount= (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }else {
                SavingAccount savingAccount= (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
            }
        }).collect(Collectors.toList());
        return bankAccountDtos;
    }

    @Override
    public CustomerDto getCustomer(Long id) throws CustomerNotFoundExcep {
        Customer customer=customerRepository.findById(id).orElseThrow(()->new CustomerNotFoundExcep("Customer not found exception"));
        CustomerDto customerDto =dtoMapper.formCustomer(customer);
        return customerDto;
    }
    @Override
    public  CustomerDto updateCustomer(CustomerDto customerDto) {
        log.info("update new customer");
        Customer customer=dtoMapper.fromCustomerDto(customerDto);
        Customer savedCustomer=customerRepository.save(customer);
        return dtoMapper.formCustomer(savedCustomer);
    }
    @Override
    public void deleteCustomer(Long id){
        customerRepository.deleteById(id);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
       List<AccountOperation> operations=accountOperationRepository.findByBankAccountId(accountId);
       return operations.stream().map(operation->dtoMapper.fromAccountOperation(operation)).collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDto getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundExcept {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        if (bankAccount==null) throw new BankAccountNotFoundExcept("Account not found");
        List<AccountOperation> operations = accountOperationRepository.findByBankAccountId(accountId, PageRequest.of(page, size));
        List<AccountOperationDTO> operationsDto = operations.stream().map(op -> dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
        AccountHistoryDto accountHistoryDto=new AccountHistoryDto();
        accountHistoryDto.setAccountId(bankAccount.getId());
        accountHistoryDto.setAccountOperationDTOS(operationsDto);
        accountHistoryDto.setAmount(bankAccount.getBalance());
        accountHistoryDto.setCurrentPage(page);
        accountHistoryDto.setPageSize(size);
        return accountHistoryDto;
    }

    @Override
    public List<CustomerDto> searchCustomers(String keyword) {
        List<Customer> customers=customerRepository.searchCostumerByName(keyword);
        return customers.stream().map(costumer -> dtoMapper.formCustomer(costumer)).collect(Collectors.toList());
    }
}

