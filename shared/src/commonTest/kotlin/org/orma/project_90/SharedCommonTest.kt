package org.orma.project_90

import kotlin.test.Test
import kotlin.test.assertEquals
import org.orma.project_90.designsystem.OrmaStatusTone
import org.orma.project_90.designsystem.ormaStatusToneFor
import org.orma.project_90.onboarding.AuthIdentifierType
import org.orma.project_90.onboarding.BusinessSetupDraft
import org.orma.project_90.onboarding.BusinessSetupStep
import org.orma.project_90.onboarding.canContinueBusinessSetup
import org.orma.project_90.onboarding.isLoginIdentifierValid
import org.orma.project_90.onboarding.isOtpValid

class SharedCommonTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun statusToneMappingUsesCentralDesignSystem() {
        assertEquals(OrmaStatusTone.Success, ormaStatusToneFor("paid"))
        assertEquals(OrmaStatusTone.Warning, ormaStatusToneFor("pending"))
        assertEquals(OrmaStatusTone.Danger, ormaStatusToneFor("failed"))
        assertEquals(OrmaStatusTone.Neutral, ormaStatusToneFor("unknown"))
    }

    @Test
    fun loginIdentifierValidationSupportsPhoneAndEmail() {
        assertEquals(true, isLoginIdentifierValid(AuthIdentifierType.Phone, "98765 43210"))
        assertEquals(false, isLoginIdentifierValid(AuthIdentifierType.Phone, "123"))
        assertEquals(true, isLoginIdentifierValid(AuthIdentifierType.Email, "owner@orma.com"))
        assertEquals(false, isLoginIdentifierValid(AuthIdentifierType.Email, "owner"))
    }

    @Test
    fun otpValidationRequiresSixDigits() {
        assertEquals(true, isOtpValid("123456"))
        assertEquals(false, isOtpValid("12345"))
    }

    @Test
    fun businessDetailsStepRequiresOwnerAndBusinessIdentity() {
        val incomplete = BusinessSetupDraft(ownerName = "Asha", businessName = "Orma Retail")
        val complete = incomplete.copy(legalName = "Orma Retail LLC")

        assertEquals(false, canContinueBusinessSetup(BusinessSetupStep.BusinessDetails, incomplete))
        assertEquals(true, canContinueBusinessSetup(BusinessSetupStep.BusinessDetails, complete))
    }
}
