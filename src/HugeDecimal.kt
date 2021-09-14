class HugeDecimal(input: String) {

    private val stringRepresentation = input

    init {
        if (verifyInput(input)) {

        } else
            throw IllegalInputFormatException("Input string has incorrect format: $input")
    }

    fun setMantissaLength(length: Int) {
        mantissaLength = length
    }

    fun add(number: HugeDecimal): HugeDecimal {

    }

    fun subtract(number: HugeDecimal): HugeDecimal {

    }

    fun multiply(number: HugeDecimal): HugeDecimal {

    }

    private fun verifyInput(line: String): Boolean {
        val regexPattern = Regex("^(\\d+|\\d+\\.\\d+)\$")
        val matches = regexPattern.find(line)
        return matches != null
    }

    companion object Config {
        private var mantissaLength: Int = 5
    }
}