import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class HugeDecimal(input: String) {

    private var listRepresentation: MutableList<Char> //Представление числа в виде изменяемого (mutable) списка
    var isNegative: Boolean //Отрицательное число или нет
    private var dotIndex: Int //Индекс разделительной точки
    lateinit var mantissa: MutableList<Char> //Мантисса числа
    var exponent: Int = 0 //Экспонента
    private var stringRepresentation: String = ""
    var isZero: Boolean = false

    init { //Конструктор нашего класса, тут делаем всякие проверки и прочие гадости
        if (verifyInput(input)) { //Проверяем корректность входной строки
            listRepresentation = input.toMutableList() //Преобразуем строку в список для удобства
            isNegative = listRepresentation[0] == '-' //Узнаем знак числа
            listRepresentation.remove('-') //Удаляем минус (мы уже знаем знак числа, в списке минус будет мешать)
            dotIndex = listRepresentation.indexOf('.') //Узнаем, какой индекс имеет разделительная точка
            dropZeros() //Отбрасываем незначащие нули дробной части
            if (representationMode == RepresentationMode.NORMALIZED)
                getNormalizedMantissaAndExponent()
            round()
            dropMantissaZeros()
            if (mantissa.count() == 1 && mantissa[0] == '0')
                isZero = true
            buildStringRepresentation()
        } else
            throw IllegalInputFormatException("Input string has incorrect format: $input")
    }

    override fun toString(): String { //Перегружаем функцию преобразования объекта нашего класса в строку для вывода в консоль
        return stringRepresentation
    }

    private fun verifyInput(line: String): Boolean { //Метод проверки входной строки на соответствие формату дробного числа
        val regexPattern = Regex("^-?(\\d+|\\d+\\.\\d+)\$")
        val matches = regexPattern.find(line)
        return matches != null
    }

    private fun dropZeros() { //Метод для отбрасывания незначащих нулей дробной части числа
        if (dotIndex != -1) { //Если индекс разделительной
            val stringRepresentation = listRepresentation.joinToString("") //Преобразуем список с цифрами в строку
            val zerosRegex = Regex("\\.?(?<=\\.|\\d)0*\$") //Регулярка найдет незначащие нули
            val stringResult = zerosRegex.replace(stringRepresentation, "") //Заменяем незначащие нули пустыми символами
            listRepresentation =
                stringResult.toMutableList() //Преобразуем получившуюся строку в список и подменяем изначальный список
        } else return
    }

    private fun dropLeadingZeros(string: String): String {

        var lastNotNullIndex = string.indexOfFirst { it != '0' }

        if (string[lastNotNullIndex] == '.')
            lastNotNullIndex -= 1

        return string.substring(lastNotNullIndex, string.length)

    }

    private fun dropMantissaZeros() { //Метод для отбрасывания незначащих нулей дробной части числа
        val stringRepresentation = mantissa.joinToString("") //Преобразуем список с цифрами в строку
        val zerosRegex = Regex("\\.?(?<=\\.|\\d)0*\$") //Регулярка найдет незначащие нули
        val stringResult = zerosRegex.replace(stringRepresentation, "") //Заменяем незначащие нули пустыми символами
        mantissa =
            stringResult.toMutableList() //Преобразуем получившуюся строку в список и подменяем изначальный список
    }

    private fun dropAnyMantissaZeros(mantissa: MutableList<Char>): MutableList<Char> { //Метод для отбрасывания незначащих нулей дробной части числа
        val stringRepresentation = mantissa.joinToString("") //Преобразуем список с цифрами в строку
        val zerosRegex = Regex("\\.?(?<=\\.|\\d)0*\$") //Регулярка найдет незначащие нули
        val stringResult = zerosRegex.replace(stringRepresentation, "") //Заменяем незначащие нули пустыми символами
        return stringResult.toMutableList()
    }

    private fun getNormalizedMantissaAndExponent() { //Метод выделения мантиссы и порядка (экспоненты) в нормализованном виде
        if (listRepresentation[0] == '0' && listRepresentation.indexOf('.') == 1) { //если первая цифра числа - ноль
            val firstNotNullIndex =
                listRepresentation.indexOfFirst { it != '0' && it != '.' } //получаем индекс первой ненулевой цифры
            exponent = dotIndex - firstNotNullIndex //получаем экспоненту
            mantissa = listRepresentation.toMutableList()
            Collections.rotate(listRepresentation, -firstNotNullIndex)
            listRepresentation.remove('.')
            listRepresentation.add(1, '.')
            dropZeros()
            val buffer = mantissa.toMutableList()
            mantissa = listRepresentation.toMutableList()
            listRepresentation = buffer.toMutableList()
        } else if (dotIndex != -1 && listRepresentation.count() > 1) {
            exponent = dotIndex - 1
            mantissa = listRepresentation.toMutableList()
            mantissa.remove('.')
            mantissa.add(1, '.')
        } else if (dotIndex == -1 && listRepresentation.count() > 1) {
            exponent = listRepresentation.count() - 1
            mantissa = listRepresentation.toMutableList()
            mantissa.add(1, '.')
        } else if (listRepresentation.count() == 1) {
            mantissa = listRepresentation.toMutableList()
            exponent = 0
        }
    }

    //TODO реализовать ненормализованные числа
    private fun getDenormalizedMantissaAndExponent() { //Метод выделения мантиссы и порядка (экспоненты) в ненормализованном виде

    }

    //TODO последние два способа округления реализованы плохо, перереализовать
    private fun round() { //Метод округления числа (если мантиссы не хватает)
        if (mantissa.count() > mantissaLength + 1) {
            if (Config.roundingMode == RoundingMode.FLOOR) { //Режим отбрасывания
                mantissa = mantissa.take(mantissaLength + 1).toMutableList()
            }
            if (Config.roundingMode == RoundingMode.HALF_UP) { //Округление вверх
                if (mantissa[mantissaLength + 2].toInt() >= 5) {
                    mantissa = mantissa.take(mantissaLength + 1).toMutableList()
                    mantissa[mantissaLength + 1] = mantissa[mantissaLength + 1].toInt().inc().toChar()
                } else
                    mantissa = mantissa.take(mantissaLength + 1).toMutableList()
            }
            if (Config.roundingMode == RoundingMode.HALF_DOWN) { //Округление вниз
                if (mantissa[mantissaLength + 2].toInt() >= 5) {
                    mantissa = mantissa.take(mantissaLength + 1).toMutableList()
                    mantissa[mantissaLength + 1] = mantissa[mantissaLength + 1].toInt().dec().toChar()
                } else
                    mantissa = mantissa.take(mantissaLength + 1).toMutableList()
            }

        }
    }

    private fun buildStringRepresentation() {
        var mantissaCopy = mantissa.toMutableList()
        mantissaCopy.remove('.')
        if (exponent < 0) {
            for (i in 1..kotlin.math.abs(exponent))
                mantissaCopy.add(0, '0')
            mantissaCopy.add(1, '.')
        } else {
            if (exponent > 0) {
                if (mantissaCopy.count() < exponent + 1)
                    while (mantissaCopy.count() - 1 != exponent)
                        mantissaCopy.add('0')
                mantissaCopy.add(exponent + 1, '.')
                mantissaCopy = dropAnyMantissaZeros(mantissaCopy)
            }
        }
        if (exponent == 0) {
            mantissaCopy.add(1, '.')
            mantissaCopy = dropAnyMantissaZeros(mantissaCopy)
        }


        if (isNegative)
            mantissaCopy.add(0, '-')
        stringRepresentation = mantissaCopy.joinToString("")
    }

    operator fun plus(other: HugeDecimal): HugeDecimal {

        if (this.isZero)
            return other
        if (other.isZero)
            return this

        if (!this.isNegative && other.isNegative)
            return this.minus(other)

        if (!other.isNegative && this.isNegative)
            return other.minus(this)

        val bothNegative = this.isNegative && other.isNegative

        equalizeFractionalPartLength(this, other)
        equalizeWholePartLength(this, other)

        val fractionPartLength = this.getFractionPartLength()

        val reversedFirstString = this.stringRepresentation.reversed().toMutableList()
        val reversedSecondString = other.stringRepresentation.reversed().toMutableList()

        reversedFirstString.remove('-')
        reversedSecondString.remove('-')
        reversedFirstString.remove('.')
        reversedSecondString.remove('.')

        val resultString = mutableListOf<Char>()
        var overflow = 0

        for (i in 0 until reversedFirstString.count()) {

            val firstDigit = reversedFirstString[i].digitToInt()
            val secondDigit = reversedSecondString[i].digitToInt()

            val preresult = firstDigit + secondDigit + overflow

            overflow = preresult / 10

            resultString.add(0, (preresult % 10 + 48).toChar())
        }

        if (fractionPartLength != 0)
            resultString.add(resultString.count() - fractionPartLength, '.')

        if (overflow != 0)
            resultString.add(0, (overflow + 48).toChar())

        if (bothNegative)
            resultString.add(0, '-')

        this.buildStringRepresentation()
        other.buildStringRepresentation()

        return HugeDecimal(resultString.joinToString(""))
    }

    operator fun minus(other: HugeDecimal): HugeDecimal {

        if (this.isNegative && other.isNegative)
            return this + other.negate()

        equalizeFractionalPartLength(this, other)
        equalizeWholePartLength(this, other)

        val whosGreater = negativeGreaterAbs(this, other)

        if (whosGreater == 0)
            return HugeDecimal("0")

        if (whosGreater == -1)
            return subtract(this, other)
        if (whosGreater == 1)
            return subtract(other, this).negate()



        return HugeDecimal("0")
    }

    private fun subtract(first: HugeDecimal, second: HugeDecimal): HugeDecimal {

        val fractionPartLength = this.getFractionPartLength()
        val reversedFirstString = first.stringRepresentation.reversed().toMutableList()
        val reversedSecondString = second.stringRepresentation.reversed().toMutableList()

        reversedFirstString.remove('-')
        reversedSecondString.remove('-')
        reversedFirstString.remove('.')
        reversedSecondString.remove('.')

        var overflow = 0
        val resultString = mutableListOf<Char>()

        for (i in 0 until reversedFirstString.count()) {

            val firstDigit = reversedFirstString[i].digitToInt()
            val secondDigit = reversedSecondString[i].digitToInt()

            var preresult = firstDigit - secondDigit - overflow

            overflow = 0

            if (preresult < 0) {
                overflow = 1
                preresult += 10
            }

            resultString.add(0, (preresult % 10 + 48).toChar())
        }

        this.buildStringRepresentation()
        second.buildStringRepresentation()

        if (fractionPartLength != 0)
            resultString.add(resultString.count() - fractionPartLength, '.')

        val result = dropLeadingZeros(resultString.joinToString(""))

        return HugeDecimal(result)

    }

    operator fun times(other: HugeDecimal): HugeDecimal {
        val thisBigDecimal = BigDecimal(this.stringRepresentation)
        val otherBigDecimal = BigDecimal(other.stringRepresentation)
        val mult = thisBigDecimal * otherBigDecimal
        return HugeDecimal(mult.toString())
    }

    fun exponentialView(): String {
        var result = mantissa.joinToString("") + "*$base^$exponent"
        if (isNegative)
            result = "-$result"
        return result
    }

    private fun negate(): HugeDecimal {
        return if (this.isNegative) {
            val buffer = this.stringRepresentation.toMutableList()
            buffer.remove('-')
            HugeDecimal(buffer.joinToString(""))
        } else {
            HugeDecimal('-' + this.stringRepresentation)
        }
    }

    private fun getFractionPartLength(): Int {
        val dotIndex = stringRepresentation.toMutableList().indexOf('.')
        return if (dotIndex == -1)
            0
        else
            stringRepresentation.length - dotIndex - 1
    }

    private fun getWholePartLength(): Int {
        return if (stringRepresentation[0] == '0')
            1
        else {
            val fractionPartLength = this.getFractionPartLength()
            stringRepresentation.length - fractionPartLength - 1
        }
    }

    private fun equalizeFractionalPartLength(first: HugeDecimal, second: HugeDecimal) {

        var leftStringRepresentation = first.stringRepresentation.toMutableList()
        var otherStringRepresentation = second.stringRepresentation.toMutableList()

        val leftFractionPartLength = first.getFractionPartLength()
        val otherFractionPartLength = second.getFractionPartLength()

        if (leftFractionPartLength < otherFractionPartLength) {
            if (leftFractionPartLength == 0)
                leftStringRepresentation.add('.')
            val diff = otherFractionPartLength - leftFractionPartLength
            for (i in 1..diff)
                leftStringRepresentation.add('0')

            first.stringRepresentation = leftStringRepresentation.joinToString("")
        }

        if (leftFractionPartLength > otherFractionPartLength) {
            if (otherFractionPartLength == 0)
                otherStringRepresentation.add('.')
            val diff = leftFractionPartLength - otherFractionPartLength
            for (i in 1..diff)
                otherStringRepresentation.add('0')

            second.stringRepresentation = otherStringRepresentation.joinToString("")
        }


    }

    private fun equalizeWholePartLength(first: HugeDecimal, second: HugeDecimal) {
        var leftStringRepresentation = first.stringRepresentation.toMutableList()
        var otherStringRepresentation = second.stringRepresentation.toMutableList()

        val leftWholePartLength = first.getWholePartLength()
        val otherWholePartLength = second.getWholePartLength()

        if (leftWholePartLength < otherWholePartLength) {
            val diff = otherWholePartLength - leftWholePartLength
            for (i in 1..diff)
                leftStringRepresentation.add(0, '0')

            first.stringRepresentation = leftStringRepresentation.joinToString("")
        }

        if (leftWholePartLength > otherWholePartLength) {
            val diff = leftWholePartLength - otherWholePartLength
            for (i in 1..diff)
                otherStringRepresentation.add(0, '0')

            second.stringRepresentation = otherStringRepresentation.joinToString("")
        }
    }

    private fun negativeGreaterAbs(first: HugeDecimal, second: HugeDecimal): Int {
        val firstStringCopy = first.stringRepresentation.toMutableList()
        val secondStringCopy = second.stringRepresentation.toMutableList()

        firstStringCopy.remove('-')
        firstStringCopy.remove('.')
        secondStringCopy.remove('-')
        secondStringCopy.remove('.')


        var done = false
        var i = 0
        while (!done) {
            if (firstStringCopy[i].code - 48 > secondStringCopy[i].code - 48) {
                return -1
            }
            if (firstStringCopy[i].code - 48 < secondStringCopy[i].code - 48) {
                return 1
            }

            if (i == firstStringCopy.count() - 1)
                return 0
            i++
        }
        return 0
    }


    companion object Config {
        private var mantissaLength: Int = 5 //Длина мантиссы
        private val base: Int = 10 //Основание системы счисления
        private var roundingMode: RoundingMode = RoundingMode.FLOOR //Режим округления
        private var representationMode =
            RepresentationMode.NORMALIZED //Способ представления числа (нормализованный или нет)

        fun setRoundingMode(roundingMode: RoundingMode) { //Метод установки режима округления
            Config.roundingMode = roundingMode
        }

        fun setRepresentationMode(representationMode: RepresentationMode) { //Метод установки способа представления числа
            Config.representationMode = representationMode
        }

        fun setMantissaLength(newLength: Int) {
            mantissaLength = newLength
        }

        fun printAllNumbersForTask4() {
            var a = HugeDecimal("0")
            var i = 0

            File("mantissa2.txt").writeText("")
            File("mantissa3.txt").writeText("")

            val firstList = mutableSetOf<HugeDecimal>()
            val secondList = mutableSetOf<HugeDecimal>()

            while (i < 100000) {
                if ((a.mantissa.count() - 1) == 2 && a.exponent == 0) {
                    File("mantissa2.txt").appendText(a.toString() + "\n")
                    firstList.add(a)
                }
                if ((a.mantissa.count() - 1) == 3 && a.exponent == 0) {
                    File("mantissa3.txt").appendText(a.toString() + "\n")
                    secondList.add(a)
                }
                i++
                a += HugeDecimal("0.01")
            }

            val resultList = mutableListOf<HugeDecimal>()

        }
    }
}