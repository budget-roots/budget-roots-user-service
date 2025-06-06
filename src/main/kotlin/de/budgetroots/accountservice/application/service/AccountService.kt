package de.budgetroots.accountservice.application.service

import de.budgetroots.accountservice.adapter.output.persistance.entity.toAccount
import de.budgetroots.accountservice.application.service.exception.AccountNotFoundException
import de.budgetroots.accountservice.domain.model.Account
import de.budgetroots.accountservice.domain.model.KeycloakToken
import de.budgetroots.accountservice.domain.model.command.LoginAccountCommand
import de.budgetroots.accountservice.domain.model.command.RegisterAccountCommand
import de.budgetroots.accountservice.domain.model.command.toEntity
import de.budgetroots.accountservice.domain.port.input.AccountDetailsUseCase
import de.budgetroots.accountservice.domain.port.input.AccountLoginUseCase
import de.budgetroots.accountservice.domain.port.input.AccountRegistrationUseCase
import de.budgetroots.accountservice.domain.port.output.AccountKeycloakPort
import de.budgetroots.accountservice.domain.port.output.AccountPersistencePort
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val keycloakPort: AccountKeycloakPort,
    private val accountPersistencePort: AccountPersistencePort,
) : AccountRegistrationUseCase,
    AccountLoginUseCase,
    AccountDetailsUseCase {
    override fun registerUser(command: RegisterAccountCommand): Account {
        val keycloakId = keycloakPort.register(command)
        val userEntity = command.toEntity(keycloakId)
        return accountPersistencePort.saveUser(userEntity).toAccount()
    }

    override fun login(command: LoginAccountCommand): KeycloakToken = keycloakPort.login(command)

    override fun getAccountDetails(keycloakId: String): Account =
        accountPersistencePort.findByKeycloakId(keycloakId)?.toAccount()
            ?: throw AccountNotFoundException(keycloakId)
}
