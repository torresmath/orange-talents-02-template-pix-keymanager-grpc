package br.com.torresmath.key.manager.pix.model

import javax.persistence.AttributeOverride
import javax.persistence.AttributeOverrides
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
@AttributeOverrides(
    AttributeOverride(name = "branch", column = Column(name = "account_branch", nullable = true)),
    AttributeOverride(name = "number", column = Column(name = "account_number", nullable = true)),
)
class Account(
    val branch: String,
    val number: String,
    val owner: AccountOwner
)

@Embeddable
@AttributeOverrides(
    AttributeOverride(name = "name", column = Column(name = "owner_name", nullable = true)),
    AttributeOverride(name = "cpf", column = Column(name = "owner_cpf", nullable = true)),
)
class AccountOwner(
    val name: String,
    val cpf: String
)