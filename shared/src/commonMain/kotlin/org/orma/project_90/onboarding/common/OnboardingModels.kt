package org.orma.project_90.onboarding

enum class AuthIdentifierType(
    val label: String,
    val fieldLabel: String,
) {
    Phone("Phone", "Phone number"),
    Email("Email", "Email address"),
}

enum class AuthProvider {
    PhoneOtp,
    EmailPassword,
    Google,
}

data class OrmaCountryUi(
    val id: String,
    val name: String,
    val dialCode: String,
    val flag: String,
    val placeholder: String,
    val minDigits: Int,
    val maxDigits: Int,
) {
    fun acceptsNationalNumber(value: String): Boolean {
        val digitCount = value.filter(Char::isDigit).length
        return digitCount in minDigits..maxDigits
    }
}

val OrmaSupportedCountries = listOf(
    country("IN", "India", "+91", "98765 43210", 10, 10),
    country("AE", "United Arab Emirates", "+971", "50 000 0000", 8, 9),
    country("SA", "Saudi Arabia", "+966", "50 000 0000", 8, 9),
    country("BH", "Bahrain", "+973", "3600 0000", 8, 8),
    country("KW", "Kuwait", "+965", "5000 0000", 8, 8),
    country("OM", "Oman", "+968", "9000 0000", 8, 8),
    country("QA", "Qatar", "+974", "3000 0000", 8, 8),
    country("US", "United States", "+1", "(555) 000-0000", 10, 10),
    country("GB", "United Kingdom", "+44", "7400 000000", 10, 10),
    country("AF", "Afghanistan", "+93"),
    country("AL", "Albania", "+355"),
    country("DZ", "Algeria", "+213"),
    country("AS", "American Samoa", "+1684"),
    country("AD", "Andorra", "+376"),
    country("AO", "Angola", "+244"),
    country("AI", "Anguilla", "+1264"),
    country("AG", "Antigua and Barbuda", "+1268"),
    country("AR", "Argentina", "+54"),
    country("AM", "Armenia", "+374"),
    country("AW", "Aruba", "+297"),
    country("AU", "Australia", "+61"),
    country("AT", "Austria", "+43"),
    country("AZ", "Azerbaijan", "+994"),
    country("BS", "Bahamas", "+1242"),
    country("BD", "Bangladesh", "+880"),
    country("BB", "Barbados", "+1246"),
    country("BY", "Belarus", "+375"),
    country("BE", "Belgium", "+32"),
    country("BZ", "Belize", "+501"),
    country("BJ", "Benin", "+229"),
    country("BM", "Bermuda", "+1441"),
    country("BT", "Bhutan", "+975"),
    country("BO", "Bolivia", "+591"),
    country("BA", "Bosnia and Herzegovina", "+387"),
    country("BW", "Botswana", "+267"),
    country("BR", "Brazil", "+55"),
    country("IO", "British Indian Ocean Territory", "+246"),
    country("VG", "British Virgin Islands", "+1284"),
    country("BN", "Brunei", "+673"),
    country("BG", "Bulgaria", "+359"),
    country("BF", "Burkina Faso", "+226"),
    country("BI", "Burundi", "+257"),
    country("KH", "Cambodia", "+855"),
    country("CM", "Cameroon", "+237"),
    country("CA", "Canada", "+1"),
    country("CV", "Cape Verde", "+238"),
    country("BQ", "Caribbean Netherlands", "+599"),
    country("KY", "Cayman Islands", "+1345"),
    country("CF", "Central African Republic", "+236"),
    country("TD", "Chad", "+235"),
    country("CL", "Chile", "+56"),
    country("CN", "China", "+86"),
    country("CX", "Christmas Island", "+61"),
    country("CC", "Cocos Islands", "+61"),
    country("CO", "Colombia", "+57"),
    country("KM", "Comoros", "+269"),
    country("CG", "Congo - Brazzaville", "+242"),
    country("CD", "Congo - Kinshasa", "+243"),
    country("CK", "Cook Islands", "+682"),
    country("CR", "Costa Rica", "+506"),
    country("CI", "Cote d'Ivoire", "+225"),
    country("HR", "Croatia", "+385"),
    country("CU", "Cuba", "+53"),
    country("CW", "Curacao", "+599"),
    country("CY", "Cyprus", "+357"),
    country("CZ", "Czechia", "+420"),
    country("DK", "Denmark", "+45"),
    country("DJ", "Djibouti", "+253"),
    country("DM", "Dominica", "+1767"),
    country("DO", "Dominican Republic", "+1809"),
    country("EC", "Ecuador", "+593"),
    country("EG", "Egypt", "+20"),
    country("SV", "El Salvador", "+503"),
    country("GQ", "Equatorial Guinea", "+240"),
    country("ER", "Eritrea", "+291"),
    country("EE", "Estonia", "+372"),
    country("SZ", "Eswatini", "+268"),
    country("ET", "Ethiopia", "+251"),
    country("FK", "Falkland Islands", "+500"),
    country("FO", "Faroe Islands", "+298"),
    country("FJ", "Fiji", "+679"),
    country("FI", "Finland", "+358"),
    country("FR", "France", "+33"),
    country("GF", "French Guiana", "+594"),
    country("PF", "French Polynesia", "+689"),
    country("GA", "Gabon", "+241"),
    country("GM", "Gambia", "+220"),
    country("GE", "Georgia", "+995"),
    country("DE", "Germany", "+49"),
    country("GH", "Ghana", "+233"),
    country("GI", "Gibraltar", "+350"),
    country("GR", "Greece", "+30"),
    country("GL", "Greenland", "+299"),
    country("GD", "Grenada", "+1473"),
    country("GP", "Guadeloupe", "+590"),
    country("GU", "Guam", "+1671"),
    country("GT", "Guatemala", "+502"),
    country("GG", "Guernsey", "+44"),
    country("GN", "Guinea", "+224"),
    country("GW", "Guinea-Bissau", "+245"),
    country("GY", "Guyana", "+592"),
    country("HT", "Haiti", "+509"),
    country("HN", "Honduras", "+504"),
    country("HK", "Hong Kong", "+852"),
    country("HU", "Hungary", "+36"),
    country("IS", "Iceland", "+354"),
    country("ID", "Indonesia", "+62"),
    country("IR", "Iran", "+98"),
    country("IQ", "Iraq", "+964"),
    country("IE", "Ireland", "+353"),
    country("IM", "Isle of Man", "+44"),
    country("IL", "Israel", "+972"),
    country("IT", "Italy", "+39"),
    country("JM", "Jamaica", "+1876"),
    country("JP", "Japan", "+81"),
    country("JE", "Jersey", "+44"),
    country("JO", "Jordan", "+962"),
    country("KZ", "Kazakhstan", "+7"),
    country("KE", "Kenya", "+254"),
    country("KI", "Kiribati", "+686"),
    country("XK", "Kosovo", "+383"),
    country("KG", "Kyrgyzstan", "+996"),
    country("LA", "Laos", "+856"),
    country("LV", "Latvia", "+371"),
    country("LB", "Lebanon", "+961"),
    country("LS", "Lesotho", "+266"),
    country("LR", "Liberia", "+231"),
    country("LY", "Libya", "+218"),
    country("LI", "Liechtenstein", "+423"),
    country("LT", "Lithuania", "+370"),
    country("LU", "Luxembourg", "+352"),
    country("MO", "Macau", "+853"),
    country("MG", "Madagascar", "+261"),
    country("MW", "Malawi", "+265"),
    country("MY", "Malaysia", "+60"),
    country("MV", "Maldives", "+960"),
    country("ML", "Mali", "+223"),
    country("MT", "Malta", "+356"),
    country("MH", "Marshall Islands", "+692"),
    country("MQ", "Martinique", "+596"),
    country("MR", "Mauritania", "+222"),
    country("MU", "Mauritius", "+230"),
    country("YT", "Mayotte", "+262"),
    country("MX", "Mexico", "+52"),
    country("FM", "Micronesia", "+691"),
    country("MD", "Moldova", "+373"),
    country("MC", "Monaco", "+377"),
    country("MN", "Mongolia", "+976"),
    country("ME", "Montenegro", "+382"),
    country("MS", "Montserrat", "+1664"),
    country("MA", "Morocco", "+212"),
    country("MZ", "Mozambique", "+258"),
    country("MM", "Myanmar", "+95"),
    country("NA", "Namibia", "+264"),
    country("NR", "Nauru", "+674"),
    country("NP", "Nepal", "+977"),
    country("NL", "Netherlands", "+31"),
    country("NC", "New Caledonia", "+687"),
    country("NZ", "New Zealand", "+64"),
    country("NI", "Nicaragua", "+505"),
    country("NE", "Niger", "+227"),
    country("NG", "Nigeria", "+234"),
    country("NU", "Niue", "+683"),
    country("NF", "Norfolk Island", "+672"),
    country("KP", "North Korea", "+850"),
    country("MK", "North Macedonia", "+389"),
    country("MP", "Northern Mariana Islands", "+1670"),
    country("NO", "Norway", "+47"),
    country("PK", "Pakistan", "+92"),
    country("PW", "Palau", "+680"),
    country("PS", "Palestine", "+970"),
    country("PA", "Panama", "+507"),
    country("PG", "Papua New Guinea", "+675"),
    country("PY", "Paraguay", "+595"),
    country("PE", "Peru", "+51"),
    country("PH", "Philippines", "+63"),
    country("PL", "Poland", "+48"),
    country("PT", "Portugal", "+351"),
    country("PR", "Puerto Rico", "+1787"),
    country("RO", "Romania", "+40"),
    country("RU", "Russia", "+7"),
    country("RW", "Rwanda", "+250"),
    country("RE", "Reunion", "+262"),
    country("BL", "Saint Barthelemy", "+590"),
    country("SH", "Saint Helena", "+290"),
    country("KN", "Saint Kitts and Nevis", "+1869"),
    country("LC", "Saint Lucia", "+1758"),
    country("MF", "Saint Martin", "+590"),
    country("PM", "Saint Pierre and Miquelon", "+508"),
    country("VC", "Saint Vincent and the Grenadines", "+1784"),
    country("WS", "Samoa", "+685"),
    country("SM", "San Marino", "+378"),
    country("ST", "Sao Tome and Principe", "+239"),
    country("SN", "Senegal", "+221"),
    country("RS", "Serbia", "+381"),
    country("SC", "Seychelles", "+248"),
    country("SL", "Sierra Leone", "+232"),
    country("SG", "Singapore", "+65"),
    country("SX", "Sint Maarten", "+1721"),
    country("SK", "Slovakia", "+421"),
    country("SI", "Slovenia", "+386"),
    country("SB", "Solomon Islands", "+677"),
    country("SO", "Somalia", "+252"),
    country("ZA", "South Africa", "+27"),
    country("KR", "South Korea", "+82"),
    country("SS", "South Sudan", "+211"),
    country("ES", "Spain", "+34"),
    country("LK", "Sri Lanka", "+94"),
    country("SD", "Sudan", "+249"),
    country("SR", "Suriname", "+597"),
    country("SJ", "Svalbard and Jan Mayen", "+47"),
    country("SE", "Sweden", "+46"),
    country("CH", "Switzerland", "+41"),
    country("SY", "Syria", "+963"),
    country("TW", "Taiwan", "+886"),
    country("TJ", "Tajikistan", "+992"),
    country("TZ", "Tanzania", "+255"),
    country("TH", "Thailand", "+66"),
    country("TL", "Timor-Leste", "+670"),
    country("TG", "Togo", "+228"),
    country("TK", "Tokelau", "+690"),
    country("TO", "Tonga", "+676"),
    country("TT", "Trinidad and Tobago", "+1868"),
    country("TN", "Tunisia", "+216"),
    country("TR", "Turkiye", "+90"),
    country("TM", "Turkmenistan", "+993"),
    country("TC", "Turks and Caicos Islands", "+1649"),
    country("TV", "Tuvalu", "+688"),
    country("VI", "U.S. Virgin Islands", "+1340"),
    country("UG", "Uganda", "+256"),
    country("UA", "Ukraine", "+380"),
    country("UY", "Uruguay", "+598"),
    country("UZ", "Uzbekistan", "+998"),
    country("VU", "Vanuatu", "+678"),
    country("VA", "Vatican City", "+379"),
    country("VE", "Venezuela", "+58"),
    country("VN", "Vietnam", "+84"),
    country("WF", "Wallis and Futuna", "+681"),
    country("EH", "Western Sahara", "+212"),
    country("YE", "Yemen", "+967"),
    country("ZM", "Zambia", "+260"),
    country("ZW", "Zimbabwe", "+263"),
)

val OrmaDefaultCountry = OrmaSupportedCountries.first { it.id == "IN" }

private fun country(
    id: String,
    name: String,
    dialCode: String,
    placeholder: String = "Phone number",
    minDigits: Int = 4,
    maxDigits: Int = 15 - dialCode.count(Char::isDigit),
): OrmaCountryUi =
    OrmaCountryUi(
        id = id,
        name = name,
        dialCode = dialCode,
        flag = "",
        placeholder = placeholder,
        minDigits = minDigits,
        maxDigits = maxDigits.coerceAtLeast(minDigits),
    )

fun ormaCountryById(id: String): OrmaCountryUi =
    OrmaSupportedCountries.firstOrNull { it.id == id } ?: OrmaDefaultCountry

enum class AccessPath(
    val title: String,
    val description: String,
) {
    BusinessOwner(
        title = "Business owner",
        description = "Create a business workspace, configure tax, invoices, and access.",
    ),
    TeamMember(
        title = "Team member",
        description = "Join an existing workspace with an invite or staff access code.",
    ),
}

enum class BusinessSetupStep(
    val title: String,
    val description: String,
) {
    BusinessDetails(
        title = "Business details",
        description = "Trading name, legal name, and industry.",
    ),
    TaxDetails(
        title = "GST/VAT details",
        description = "Registration status, tax number, and tax label.",
    ),
    Address(
        title = "Business address",
        description = "Registered address used on invoices.",
    ),
    Logo(
        title = "Logo upload",
        description = "Brand mark shown on invoices and documents.",
    ),
    InvoiceSettings(
        title = "Invoice settings",
        description = "Numbering, terms, and footer text.",
    ),
    CurrencyTax(
        title = "Currency and tax",
        description = "Default currency, tax behavior, and price mode.",
    ),
}

data class BusinessSetupDraft(
    val ownerName: String = "",
    val businessName: String = "",
    val legalName: String = "",
    val industry: String = "Retail",
    val website: String = "",
    val isTaxRegistered: Boolean = true,
    val taxNumber: String = "",
    val taxLabel: String = "GST/VAT",
    val addressLine: String = "",
    val city: String = "",
    val region: String = "",
    val country: String = "United Arab Emirates",
    val postalCode: String = "",
    val logoFileName: String = "",
    val invoicePrefix: String = "ORMA",
    val nextInvoiceNumber: String = "0001",
    val paymentTerms: String = "Due on receipt",
    val invoiceFooter: String = "Thank you for your business.",
    val currency: String = "AED",
    val taxMode: String = "Standard taxable",
    val pricesIncludeTax: Boolean = false,
)

val OrmaSupportedIndustries = listOf(
    "Retail",
    "Restaurant",
    "Services",
    "Wholesale",
    "Healthcare",
    "B2B",
)

val OrmaSupportedCurrencies = listOf("AED", "USD", "SAR", "INR", "EUR")

val OrmaTaxModes = listOf(
    "Standard taxable",
    "Zero rated",
    "Tax exempt",
    "Mixed catalog",
)

fun isLoginIdentifierValid(
    type: AuthIdentifierType,
    value: String,
    country: OrmaCountryUi = OrmaDefaultCountry,
): Boolean {
    val trimmed = value.trim()
    return when (type) {
        AuthIdentifierType.Phone -> country.acceptsNationalNumber(trimmed)
        AuthIdentifierType.Email -> trimmed.contains("@") && trimmed.substringAfter("@").contains(".")
    }
}

fun isOtpValid(code: String): Boolean = code.filter(Char::isDigit).length == 6

fun canContinueBusinessSetup(
    step: BusinessSetupStep,
    draft: BusinessSetupDraft,
): Boolean = when (step) {
    BusinessSetupStep.BusinessDetails -> {
        draft.ownerName.isNotBlank() && draft.businessName.isNotBlank() && draft.legalName.isNotBlank()
    }
    BusinessSetupStep.TaxDetails -> {
        !draft.isTaxRegistered || draft.taxNumber.isNotBlank()
    }
    BusinessSetupStep.Address -> {
        draft.addressLine.isNotBlank() && draft.city.isNotBlank() && draft.country.isNotBlank()
    }
    BusinessSetupStep.Logo -> true
    BusinessSetupStep.InvoiceSettings -> {
        draft.invoicePrefix.isNotBlank() && draft.nextInvoiceNumber.isNotBlank()
    }
    BusinessSetupStep.CurrencyTax -> {
        draft.currency.isNotBlank() && draft.taxMode.isNotBlank()
    }
}

fun isBusinessSetupComplete(draft: BusinessSetupDraft): Boolean =
    BusinessSetupStep.entries.all { canContinueBusinessSetup(it, draft) }
