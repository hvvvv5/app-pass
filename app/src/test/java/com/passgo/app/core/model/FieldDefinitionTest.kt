package com.passgo.app.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FieldDefinitionTest {

    // ── CreditCardNumber ─────────────────────────────────────

    @Nested
    inner class CreditCardNumberTest {

        @ParameterizedTest
        @ValueSource(strings = ["4111111111111111", "5500000000000004", "378282246310005", "6011111111111117"])
        fun `valid card numbers pass validation`(number: String) {
            val result = FieldDefinition.CreditCardNumber.validate(number)
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @ParameterizedTest
        @ValueSource(strings = ["1234", "abc", "4111111111111", "41111111111111111111"])
        fun `invalid card numbers fail validation`(number: String) {
            val result = FieldDefinition.CreditCardNumber.validate(number)
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `formats card number with spaces`() {
            assertEquals("4111 1111 1111 1111", FieldDefinition.CreditCardNumber.format("4111111111111111"))
        }

        @Test
        fun `parse strips non-digits`() {
            assertEquals("4111111111111111", FieldDefinition.CreditCardNumber.parse("4111-1111-1111-1111"))
        }
    }

    // ── CreditCardCvv ────────────────────────────────────────

    @Nested
    inner class CreditCardCvvTest {

        @ParameterizedTest
        @ValueSource(strings = ["123", "1234"])
        fun `valid CVV passes validation`(cvv: String) {
            val result = FieldDefinition.CreditCardCvv.validate(cvv)
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @ParameterizedTest
        @ValueSource(strings = ["12", "12345", "abc"])
        fun `invalid CVV fails validation`(cvv: String) {
            val result = FieldDefinition.CreditCardCvv.validate(cvv)
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── CreditCardExpiry ─────────────────────────────────────

    @Nested
    inner class CreditCardExpiryTest {

        @ParameterizedTest
        @ValueSource(strings = ["1225", "0126", "1124"])
        fun `valid expiry passes validation`(expiry: String) {
            val result = FieldDefinition.CreditCardExpiry.validate(expiry)
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @ParameterizedTest
        @ValueSource(strings = ["00", "1300", "0000", "abcd"])
        fun `invalid expiry fails validation`(expiry: String) {
            val result = FieldDefinition.CreditCardExpiry.validate(expiry)
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `formats expiry with slash`() {
            assertEquals("12/25", FieldDefinition.CreditCardExpiry.format("1225"))
        }

        @Test
        fun `parse expiry strips non-digits`() {
            assertEquals("1225", FieldDefinition.CreditCardExpiry.parse("12/25"))
        }
    }

    // ── IBAN ─────────────────────────────────────────────────

    @Nested
    inner class IbanTest {

        @ParameterizedTest
        @ValueSource(strings = ["GB33BUKB20201555555555", "DE89370400440532013000", "FR7630006000011234567890189"])
        fun `valid IBAN passes validation`(iban: String) {
            val result = FieldDefinition.Iban.validate(iban)
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @ParameterizedTest
        @ValueSource(strings = ["AB", "123456789012345"])
        fun `invalid IBAN fails validation`(iban: String) {
            val result = FieldDefinition.Iban.validate(iban)
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `formats IBAN with spaces`() {
            assertEquals("GB33 BUKB 2020 1555 5555 55", FieldDefinition.Iban.format("GB33BUKB20201555555555"))
        }
    }

    // ── ServerPort ───────────────────────────────────────────

    @Nested
    inner class ServerPortTest {

        @ParameterizedTest
        @ValueSource(strings = ["80", "443", "8080", "65535", "1"])
        fun `valid port passes validation`(port: String) {
            val result = FieldDefinition.ServerPort.validate(port)
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @ParameterizedTest
        @ValueSource(strings = ["0", "65536", "99999", "abc"])
        fun `invalid port fails validation`(port: String) {
            val result = FieldDefinition.ServerPort.validate(port)
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── DateOfBirth ─────────────────────────────────────────

    @Nested
    inner class DateOfBirthTest {

        @Test
        fun `valid date passes validation`() {
            val result = FieldDefinition.DateOfBirth.validate("19900515")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `format adds dashes`() {
            assertEquals("1990-05-15", FieldDefinition.DateOfBirth.format("19900515"))
        }
    }

    // ── ApiKey ───────────────────────────────────────────────

    @Nested
    inner class ApiKeyTest {

        @Test
        fun `valid key passes validation`() {
            val result = FieldDefinition.ApiKey.validate("abcdefgh12345678")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `short key fails validation`() {
            val result = FieldDefinition.ApiKey.validate("abc")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── WifiPassword ─────────────────────────────────────────

    @Nested
    inner class WifiPasswordTest {

        @Test
        fun `valid password passes validation`() {
            val result = FieldDefinition.WifiPassword.validate("securePass123!")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank password fails validation`() {
            val result = FieldDefinition.WifiPassword.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── CustomNumber ─────────────────────────────────────────

    @Nested
    inner class CustomNumberTest {

        @Test
        fun `valid number passes validation`() {
            val result = FieldDefinition.CustomNumber(FieldId.CUSTOM_NUMBER).validate("42.5")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `non-numeric value fails validation`() {
            val result = FieldDefinition.CustomNumber(FieldId.CUSTOM_NUMBER).validate("abc")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `empty string passes validation`() {
            val result = FieldDefinition.CustomNumber(FieldId.CUSTOM_NUMBER).validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── CreditCardPin ─────────────────────────────────────────

    @Nested
    inner class CreditCardPinTest {

        @ParameterizedTest
        @ValueSource(strings = ["1234", "123456", "0000"])
        fun `valid PIN passes validation`(pin: String) {
            val result = FieldDefinition.CreditCardPin.validate(pin)
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @ParameterizedTest
        @ValueSource(strings = ["123", "1234567"])
        fun `invalid PIN fails validation`(pin: String) {
            val result = FieldDefinition.CreditCardPin.validate(pin)
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `empty PIN passes validation as optional`() {
            val result = FieldDefinition.CreditCardPin.validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── BankName ──────────────────────────────────────────────

    @Nested
    inner class BankNameTest {

        @ParameterizedTest
        @ValueSource(strings = ["Chase", "Bank of America", "Wells Fargo"])
        fun `valid bank name passes validation`(name: String) {
            val result = FieldDefinition.BankName.validate(name)
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank bank name fails validation`() {
            val result = FieldDefinition.BankName.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── AccountHolder ─────────────────────────────────────────

    @Nested
    inner class AccountHolderTest {

        @Test
        fun `valid account holder passes validation`() {
            val result = FieldDefinition.AccountHolder.validate("John Doe")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank account holder fails validation`() {
            val result = FieldDefinition.AccountHolder.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── Currency ──────────────────────────────────────────────

    @Nested
    inner class CurrencyTest {

        @ParameterizedTest
        @ValueSource(strings = ["USD", "eur", "GBP"])
        fun `valid currency passes validation`(currency: String) {
            val result = FieldDefinition.Currency.validate(currency)
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @ParameterizedTest
        @ValueSource(strings = ["US", "USDD", "123", "$$$"])
        fun `invalid currency fails validation`(currency: String) {
            val result = FieldDefinition.Currency.validate(currency)
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `format uppercases currency`() {
            assertEquals("USD", FieldDefinition.Currency.format("usd"))
        }
    }

    // ── PayPalEmail ───────────────────────────────────────────

    @Nested
    inner class PayPalEmailTest {

        @Test
        fun `valid email passes validation`() {
            val result = FieldDefinition.PayPalEmail.validate("user@paypal.com")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank email fails validation`() {
            val result = FieldDefinition.PayPalEmail.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `email without at sign fails validation`() {
            val result = FieldDefinition.PayPalEmail.validate("invalid")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── MerchantId ────────────────────────────────────────────

    @Nested
    inner class MerchantIdTest {

        @Test
        fun `valid merchant ID passes validation`() {
            val result = FieldDefinition.MerchantId.validate("merchant_123")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank merchant ID fails validation`() {
            val result = FieldDefinition.MerchantId.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── Beneficiary ───────────────────────────────────────────

    @Nested
    inner class BeneficiaryTest {

        @Test
        fun `valid beneficiary passes validation`() {
            val result = FieldDefinition.Beneficiary.validate("Jane Smith")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank beneficiary fails validation`() {
            val result = FieldDefinition.Beneficiary.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── Branch ─────────────────────────────────────────────────

    @Nested
    inner class BranchTest {

        @Test
        fun `valid branch passes validation`() {
            val result = FieldDefinition.Branch.validate("Main Street")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `empty branch passes validation as optional`() {
            val result = FieldDefinition.Branch.validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── CustomerId ────────────────────────────────────────────

    @Nested
    inner class CustomerIdTest {

        @Test
        fun `valid customer ID passes validation`() {
            val result = FieldDefinition.CustomerId.validate("cus_abc123")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `empty customer ID passes validation as optional`() {
            val result = FieldDefinition.CustomerId.validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── Reference ─────────────────────────────────────────────

    @Nested
    inner class ReferenceTest {

        @Test
        fun `valid reference passes validation`() {
            val result = FieldDefinition.Reference.validate("INV-2024-001")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `empty reference passes validation as optional`() {
            val result = FieldDefinition.Reference.validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── PassportType ──────────────────────────────────────────

    @Nested
    inner class PassportTypeTest {

        @Test
        fun `valid passport type passes validation`() {
            val result = FieldDefinition.PassportType.validate("Standard")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank passport type fails validation`() {
            val result = FieldDefinition.PassportType.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── IssuingCountry ─────────────────────────────────────────

    @Nested
    inner class IssuingCountryTest {

        @Test
        fun `valid issuing country passes validation`() {
            val result = FieldDefinition.IssuingCountry.validate("United States")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank issuing country fails validation`() {
            val result = FieldDefinition.IssuingCountry.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── Nationality ────────────────────────────────────────────

    @Nested
    inner class NationalityTest {

        @Test
        fun `valid nationality passes validation`() {
            val result = FieldDefinition.Nationality.validate("American")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank nationality fails validation`() {
            val result = FieldDefinition.Nationality.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── PassportExpiry ─────────────────────────────────────────

    @Nested
    inner class PassportExpiryTest {

        @Test
        fun `valid expiry passes validation`() {
            val result = FieldDefinition.PassportExpiry.validate("20301215")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `invalid expiry fails validation`() {
            val result = FieldDefinition.PassportExpiry.validate("2030")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `formats expiry with dashes`() {
            assertEquals("2030-12-15", FieldDefinition.PassportExpiry.format("20301215"))
        }

        @Test
        fun `parse expiry strips non-digits`() {
            assertEquals("20301215", FieldDefinition.PassportExpiry.parse("2030-12-15"))
        }
    }

    // ── LicenseClass ──────────────────────────────────────────

    @Nested
    inner class LicenseClassTest {

        @Test
        fun `valid license class passes validation`() {
            val result = FieldDefinition.LicenseClass.validate("Class B")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `empty license class passes validation as optional`() {
            val result = FieldDefinition.LicenseClass.validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── ResidencePermitNumber ─────────────────────────────────

    @Nested
    inner class ResidencePermitNumberTest {

        @Test
        fun `valid permit number passes validation`() {
            val result = FieldDefinition.ResidencePermitNumber.validate("RP123456")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank permit number fails validation`() {
            val result = FieldDefinition.ResidencePermitNumber.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── ResidencePermitType ───────────────────────────────────

    @Nested
    inner class ResidencePermitTypeTest {

        @Test
        fun `valid permit type passes validation`() {
            val result = FieldDefinition.ResidencePermitType.validate("Work")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `empty permit type passes validation as optional`() {
            val result = FieldDefinition.ResidencePermitType.validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── ResidencePermitExpiry ─────────────────────────────────

    @Nested
    inner class ResidencePermitExpiryTest {

        @Test
        fun `valid expiry passes validation`() {
            val result = FieldDefinition.ResidencePermitExpiry.validate("20251231")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `invalid expiry fails validation`() {
            val result = FieldDefinition.ResidencePermitExpiry.validate("2025")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `formats expiry with dashes`() {
            assertEquals("2025-12-31", FieldDefinition.ResidencePermitExpiry.format("20251231"))
        }

        @Test
        fun `parse expiry strips non-digits`() {
            assertEquals("20251231", FieldDefinition.ResidencePermitExpiry.parse("2025-12-31"))
        }
    }

    // ── SocialSecurityNumber ──────────────────────────────────

    @Nested
    inner class SocialSecurityNumberTest {

        @Test
        fun `valid SSN passes validation`() {
            val result = FieldDefinition.SocialSecurityNumber.validate("123456789")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `invalid SSN fails validation`() {
            val result = FieldDefinition.SocialSecurityNumber.validate("12345")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `formats SSN with dashes`() {
            assertEquals("123-45-6789", FieldDefinition.SocialSecurityNumber.format("123456789"))
        }

        @Test
        fun `parse SSN strips non-digits`() {
            assertEquals("123456789", FieldDefinition.SocialSecurityNumber.parse("123-45-6789"))
        }
    }

    // ── TaxId ──────────────────────────────────────────────────

    @Nested
    inner class TaxIdTest {

        @Test
        fun `valid Tax ID passes validation`() {
            val result = FieldDefinition.TaxId.validate("12-3456789")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank Tax ID fails validation`() {
            val result = FieldDefinition.TaxId.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }

        @Test
        fun `format uppercases Tax ID`() {
            assertEquals("123456789", FieldDefinition.TaxId.format("12-3456789"))
        }

        @Test
        fun `parse strips non-alphanumeric`() {
            assertEquals("123456789", FieldDefinition.TaxId.parse("12-3456789"))
        }
    }

    // ── StudentIdNumber ───────────────────────────────────────

    @Nested
    inner class StudentIdNumberTest {

        @Test
        fun `valid student ID passes validation`() {
            val result = FieldDefinition.StudentIdNumber.validate("S12345")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank student ID fails validation`() {
            val result = FieldDefinition.StudentIdNumber.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── InstitutionName ───────────────────────────────────────

    @Nested
    inner class InstitutionNameTest {

        @Test
        fun `valid institution name passes validation`() {
            val result = FieldDefinition.InstitutionName.validate("MIT")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank institution name fails validation`() {
            val result = FieldDefinition.InstitutionName.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── HealthInsuranceId ─────────────────────────────────────

    @Nested
    inner class HealthInsuranceIdTest {

        @Test
        fun `valid member ID passes validation`() {
            val result = FieldDefinition.HealthInsuranceId.validate("MEM12345")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `empty member ID passes validation as optional`() {
            val result = FieldDefinition.HealthInsuranceId.validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── HealthInsuranceProvider ───────────────────────────────

    @Nested
    inner class HealthInsuranceProviderTest {

        @Test
        fun `valid provider passes validation`() {
            val result = FieldDefinition.HealthInsuranceProvider.validate("Blue Cross")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank provider fails validation`() {
            val result = FieldDefinition.HealthInsuranceProvider.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── HealthInsuranceGroupNumber ────────────────────────────

    @Nested
    inner class HealthInsuranceGroupNumberTest {

        @Test
        fun `valid group number passes validation`() {
            val result = FieldDefinition.HealthInsuranceGroupNumber.validate("GRP001")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `empty group number passes validation as optional`() {
            val result = FieldDefinition.HealthInsuranceGroupNumber.validate("")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }
    }

    // ── PolicyNumber ──────────────────────────────────────────

    @Nested
    inner class PolicyNumberTest {

        @Test
        fun `valid policy number passes validation`() {
            val result = FieldDefinition.PolicyNumber.validate("POL-12345")
            assertInstanceOf(FieldValidationResult.Valid::class.java, result)
        }

        @Test
        fun `blank policy number fails validation`() {
            val result = FieldDefinition.PolicyNumber.validate("")
            assertInstanceOf(FieldValidationResult.Invalid::class.java, result)
        }
    }

    // ── FieldDefinition.lookup ───────────────────────────────

    @Nested
    inner class LookupTest {

        @Test
        fun `fromId returns matching definition`() {
            val def = FieldDefinition.fromId(FieldId.CREDIT_CARD_NUMBER)
            assertInstanceOf(FieldDefinition.CreditCardNumber::class.java, def)
        }

        @Test
        fun `fromId returns CustomText for unknown field`() {
            val def = FieldDefinition.fromId(FieldId.CUSTOM_TEXT)
            assertTrue(def is FieldDefinition.CustomText)
        }
    }
}
