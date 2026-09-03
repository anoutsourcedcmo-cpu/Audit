package com.example.data

data class AuditQuestion(
    val id: String,
    val questionNumber: String,
    val label: String
)

data class AuditSection(
    val title: String,
    val isMacro: Boolean,
    val isStrategy: Boolean = false,
    val isOrganization: Boolean = false,
    val isSystems: Boolean = false,
    val isProductivity: Boolean = false,
    val isFunction: Boolean = false,
    val questions: List<AuditQuestion>
)

val auditSections = listOf(
    // MACROENVIRONMENT
    AuditSection(
        title = "Demographic",
        isMacro = true,
        questions = listOf(
            AuditQuestion("dem_1", "1", "What major demographic developments and trends pose opportunities or threats to this company?"),
            AuditQuestion("dem_2", "2", "What actions has the company taken in response to these developments and trends?")
        )
    ),
    AuditSection(
        title = "Economic",
        isMacro = true,
        questions = listOf(
            AuditQuestion("eco_1", "1", "What major developments in income, prices, savings, and credit will affect the company?"),
            AuditQuestion("eco_2", "2", "What actions has the company been taking in response to these developments and trends?")
        )
    ),
    AuditSection(
        title = "Ecological",
        isMacro = true,
        questions = listOf(
            AuditQuestion("ecl_1", "1", "What is the outlook for the cost and availability of natural resources and energy needed by the company?"),
            AuditQuestion("ecl_2", "2", "What concerns have been expressed about the company's role in pollution and conservation, and what steps has the company taken?")
        )
    ),
    AuditSection(
        title = "Technological",
        isMacro = true,
        questions = listOf(
            AuditQuestion("tec_1", "1", "What major changes are occurring in product technology? In process technology? What is the company's position in these technologies?"),
            AuditQuestion("tec_2", "2", "What major generic substitutes might replace this product?")
        )
    ),
    AuditSection(
        title = "Political",
        isMacro = true,
        questions = listOf(
            AuditQuestion("pol_1", "1", "What laws are being proposed that could affect marketing strategy and tactics?"),
            AuditQuestion("pol_2", "2", "What federal, state, and local actions should be watched? What is happening in the areas of pollution control, equal employment opportunity, product safety, advertising, price control, and so forth, that affects marketing strategy?")
        )
    ),
    AuditSection(
        title = "Cultural",
        isMacro = true,
        questions = listOf(
            AuditQuestion("cul_1", "1", "What is the public's attitude toward business and toward the products produced by the company?"),
            AuditQuestion("cul_2", "2", "What changes in consumer and business lifestyles and values have a bearing on the company?")
        )
    ),
    // TASK ENVIRONMENT
    AuditSection(
        title = "Markets",
        isMacro = false,
        questions = listOf(
            AuditQuestion("mkt_1", "1", "What is happening to market size, growth, geographical distribution, and profits?"),
            AuditQuestion("mkt_2", "2", "What are the major market segments?")
        )
    ),
    AuditSection(
        title = "Customers",
        isMacro = false,
        questions = listOf(
            AuditQuestion("cst_1", "1", "How do customers and prospects rate the company and its competitors on reputation, product quality, service, salesforce, and price?"),
            AuditQuestion("cst_2", "2", "How do different customer segments make their buying decisions?")
        )
    ),
    AuditSection(
        title = "Competitors",
        isMacro = false,
        questions = listOf(
            AuditQuestion("cmp_1", "1", "Who are the major competitors? What are their objectives and strategies, their strengths and weaknesses, their sizes and market shares?"),
            AuditQuestion("cmp_2", "2", "What trends will affect future competition and substitutes for this product?")
        )
    ),
    AuditSection(
        title = "Distribution and Dealers",
        isMacro = false,
        questions = listOf(
            AuditQuestion("dst_1", "1", "What are the trade channels for bringing products to customers?"),
            AuditQuestion("dst_2", "2", "What are the efficiency levels and growth potentials of the different trade channels?")
        )
    ),
    AuditSection(
        title = "Suppliers",
        isMacro = false,
        questions = listOf(
            AuditQuestion("sup_1", "1", "What is the outlook for the availability of key resources used in production?"),
            AuditQuestion("sup_2", "2", "What trends are occurring among suppliers in their pattern of selling?")
        )
    ),
    AuditSection(
        title = "Facilitators and Marketing Firms",
        isMacro = false,
        questions = listOf(
            AuditQuestion("fac_1", "1", "What is the cost and availability outlook for transportation services?"),
            AuditQuestion("fac_2", "2", "What is the cost and availability outlook for warehousing facilities?"),
            AuditQuestion("fac_3", "3", "What is the cost and availability outlook for financial resources?"),
            AuditQuestion("fac_4", "4", "How effective are the company's advertising agencies and marketing research firms?")
        )
    ),
    AuditSection(
        title = "Publics",
        isMacro = false,
        questions = listOf(
            AuditQuestion("pub_1", "1", "What publics represent particular opportunities or problems for the company?"),
            AuditQuestion("pub_2", "2", "What steps has the company taken to deal effectively with each public?")
        )
    ),
    // MARKETING STRATEGY EVALUATION OF YOUR ORGANIZATION
    AuditSection(
        title = "Business Mission",
        isMacro = false,
        isStrategy = true,
        questions = listOf(
            AuditQuestion("str_mis_1", "1", "Is the business mission clearly stated in market-oriented terms? Is it feasible?")
        )
    ),
    AuditSection(
        title = "Marketing Objectives and Goals",
        isMacro = false,
        isStrategy = true,
        questions = listOf(
            AuditQuestion("str_obj_1", "1", "Are the corporate and marketing objectives stated in the form of clear goals to guide marketing planning and performance measurement?"),
            AuditQuestion("str_obj_2", "2", "Are the marketing objectives appropriate, given the company's competitive position, resources, and opportunities?")
        )
    ),
    AuditSection(
        title = "Strategy",
        isMacro = false,
        isStrategy = true,
        questions = listOf(
            AuditQuestion("str_str_1", "1", "Is management able to articulate a clear marketing strategy for achieving its marketing objectives? Is the strategy convincing? Is the strategy appropriate to the stage of the product life cycle, competitors' strategies, and the state of the economy?"),
            AuditQuestion("str_str_2", "2", "Is the company using the best basis for market segmentation? Does it have sound criteria for rating the segments and choosing the best ones? Has it developed accurate profiles of each target segment?"),
            AuditQuestion("str_str_3", "3", "Has the company developed a sound positioning and marketing mix for each target segment? Are marketing resources allocated optimally to the major elements of the marketing mix—i.e., product quality, service, sales force, advertising, promotion, and distribution?"),
            AuditQuestion("str_str_4", "4", "Are enough resources or too many resources budgeted to accomplish the marketing objectives?")
        )
    ),
    // MARKETING ORGANIZATION EVALUATION
    AuditSection(
        title = "Formal Structure",
        isMacro = false,
        isOrganization = true,
        questions = listOf(
            AuditQuestion("org_str_1", "1", "Does the marketing officer have adequate authority over, and responsibility for, company activities that affect the customer's satisfaction?"),
            AuditQuestion("org_str_2", "2", "Are the marketing activities optimally structured along functional, product, end-user, and territorial lines?")
        )
    ),
    AuditSection(
        title = "Functional Efficiency",
        isMacro = false,
        isOrganization = true,
        questions = listOf(
            AuditQuestion("org_eff_1", "1", "Are there good communication and working relations between marketing and sales?"),
            AuditQuestion("org_eff_2", "2", "Is the product management system working effectively? Are product managers able to plan profits or only sales volume?"),
            AuditQuestion("org_eff_3", "3", "Are there any groups in marketing that need more training, motivation, supervision, or evaluation?")
        )
    ),
    AuditSection(
        title = "Interface Efficiency",
        isMacro = false,
        isOrganization = true,
        questions = listOf(
            AuditQuestion("org_int_1", "1", "Are there any problems between marketing and manufacturing, R&D, purchasing, finance, accounting, and legal that need attention?")
        )
    ),
    // MARKETING SYSTEMS AUDIT
    AuditSection(
        title = "Marketing Information System",
        isMacro = false,
        isSystems = true,
        questions = listOf(
            AuditQuestion("sys_inf_1", "1", "Is the marketing intelligence system producing accurate, sufficient, and timely information about marketplace developments with respect to customers, prospects, distributors and dealers, competitors, suppliers, and various publics?"),
            AuditQuestion("sys_inf_2", "2", "Are company decision makers asking for enough marketing research, and are they using the results?"),
            AuditQuestion("sys_inf_3", "3", "Is the company employing the best methods for market and sales forecasting?")
        )
    ),
    AuditSection(
        title = "Marketing Planning Systems",
        isMacro = false,
        isSystems = true,
        questions = listOf(
            AuditQuestion("sys_pla_1", "1", "Is the marketing planning system well conceived and effective?"),
            AuditQuestion("sys_pla_2", "2", "Is sales forecasting and market potential measurement soundly carried out?"),
            AuditQuestion("sys_pla_3", "3", "Are sales quotas set on a proper basis?")
        )
    ),
    AuditSection(
        title = "Marketing Control System",
        isMacro = false,
        isSystems = true,
        questions = listOf(
            AuditQuestion("sys_con_1", "1", "Are the control procedures adequate to ensure that the annual-plan objectives are being achieved?"),
            AuditQuestion("sys_con_2", "2", "Does management periodically analyze the profitability of products, markets, territories, and channels of distribution?"),
            AuditQuestion("sys_con_3", "3", "Are marketing costs periodically examined?")
        )
    ),
    AuditSection(
        title = "New-Product-Development System",
        isMacro = false,
        isSystems = true,
        questions = listOf(
            AuditQuestion("sys_dev_1", "1", "Is the company well organized to gather, generate, and screen new-product ideas?"),
            AuditQuestion("sys_dev_2", "2", "Does the company do adequate concept research and business analysis before investing in new ideas?"),
            AuditQuestion("sys_dev_3", "3", "Does the company carry out adequate product and market testing before launching new products?")
        )
    ),
    // MARKETING PRODUCTIVITY EVALUATION
    AuditSection(
        title = "Profitability Analysis",
        isMacro = false,
        isProductivity = true,
        questions = listOf(
            AuditQuestion("prod_prf_1", "1", "What is the profitability of the company's different products, markets, territories, and channels of distribution?"),
            AuditQuestion("prod_prf_2", "2", "Should the company enter, expand, contract, or withdraw from any business segments and what would be the short- and long-run profit consequences?")
        )
    ),
    AuditSection(
        title = "Cost-Effectiveness Analysis",
        isMacro = false,
        isProductivity = true,
        questions = listOf(
            AuditQuestion("prod_cst_1", "1", "Do any marketing activities seem to have excessive costs? Can cost-reducing steps be taken?")
        )
    ),
    // MARKETING FUNCTION EVALUATION
    AuditSection(
        title = "Products",
        isMacro = false,
        isFunction = true,
        questions = listOf(
            AuditQuestion("fun_prd_1", "1", "What are the product-line objectives? Are these objectives sound? Is the current product line meeting the objectives?"),
            AuditQuestion("fun_prd_2", "2", "Should the product line be stretched or contracted upward, downward, or both ways?"),
            AuditQuestion("fun_prd_3", "3", "Which products should be phased out? Which products should be added?"),
            AuditQuestion("fun_prd_4", "4", "What is the buyers' knowledge and attitudes toward the company's and competitors' product quality, features, styling, brand names, etc.? What areas of product strategy need improvement?")
        )
    ),
    AuditSection(
        title = "Price",
        isMacro = false,
        isFunction = true,
        questions = listOf(
            AuditQuestion("fun_prc_1", "1", "What are the pricing objectives, policies, strategies, and procedures? To what extent are prices set on cost, demand, and competitive criteria?"),
            AuditQuestion("fun_prc_2", "2", "Do the customers see the company's prices as being in line with the value of its offer?"),
            AuditQuestion("fun_prc_3", "3", "What does management know about the price elasticity of demand, experience curve effects, and competitors' prices and pricing policies?"),
            AuditQuestion("fun_prc_4", "4", "To what extent are price policies compatible with the needs of distributors and dealers, suppliers, and government regulation?")
        )
    ),
    AuditSection(
        title = "Distribution",
        isMacro = false,
        isFunction = true,
        questions = listOf(
            AuditQuestion("fun_dst_1", "1", "What are the distribution objectives and strategies?"),
            AuditQuestion("fun_dst_2", "2", "Is there adequate market coverage and service?"),
            AuditQuestion("fun_dst_3", "3", "How effective are the following channel members: distributors, dealers, manufacturers' representatives, brokers, agents, etc.?"),
            AuditQuestion("fun_dst_4", "4", "Should the company consider changing its distribution channels?")
        )
    ),
    AuditSection(
        title = "Advertising, Sales Promotion, and Publicity",
        isMacro = false,
        isFunction = true,
        questions = listOf(
            AuditQuestion("fun_adv_1", "1", "What are the organization's advertising objectives? Are they sound?"),
            AuditQuestion("fun_adv_2", "2", "Is the right amount being spent on advertising? How is the budget determined?"),
            AuditQuestion("fun_adv_3", "3", "Are the ad themes and copy effective? What do customers and the public think about the advertising?"),
            AuditQuestion("fun_adv_4", "4", "Are the advertising media well chosen?"),
            AuditQuestion("fun_adv_5", "5", "Is the internal advertising staff adequate?"),
            AuditQuestion("fun_adv_6", "6", "Is the sales-promotion budget adequate? Is there effective and sufficient use of sales-promotion tools such as samples, coupons, displays, sales contests?"),
            AuditQuestion("fun_adv_7", "7", "Is the publicity budget adequate? Is the public-relations staff competent and creative?")
        )
    ),
    AuditSection(
        title = "Salesforce",
        isMacro = false,
        isFunction = true,
        questions = listOf(
            AuditQuestion("fun_slf_1", "1", "What are the organization's salesforce objectives?"),
            AuditQuestion("fun_slf_2", "2", "Is the salesforce large enough to accomplish the company's objectives?"),
            AuditQuestion("fun_slf_3", "3", "Is the salesforce organized along the proper principles of specialization (territory, market, product)? Are there enough (or too many) sales managers to guide the field sales representatives?"),
            AuditQuestion("fun_slf_4", "4", "Does the sales-compensation level and structure provide adequate incentive and reward?"),
            AuditQuestion("fun_slf_5", "5", "Does the salesforce show high morale, ability, and effort?"),
            AuditQuestion("fun_slf_6", "6", "Are the procedures adequate for setting quotas and evaluating performances?"),
            AuditQuestion("fun_slf_7", "7", "How does the company's salesforce compare to competitors' salesforces?")
        )
    )
)
