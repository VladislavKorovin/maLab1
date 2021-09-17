import java.util.*

class HugeDecimal(input: String) {

    private var listRepresentation: MutableList<Char> //Представление числа в виде изменяемого (mutable) списка
    private var isNegative: Boolean //Отрицательное число или нет
    private var dotIndex: Int //Индекс разделительной точки
    private lateinit var mantissa: MutableList<Char> //Мантисса числа
    private var exponent: Int = 0 //Экспонента

    init { //Конструктор нашего класса, тут делаем всякие проверки и прочие гадости
        if (verifyInput(input)) { //Проверяем корректность входной строки
            listRepresentation = input.toMutableList() //Преобразуем строку в список для удобства
            isNegative = listRepresentation[0] == '-' //Узнаем знак числа
            listRepresentation.remove('-') //Удаляем минус (мы уже знаем знак числа, в списке минус будет мешать)
            dropZeros() //Отбрасываем незначащие нули дробной части
            dotIndex = listRepresentation.indexOf('.') //Узнаем, какой индекс имеет разделительная точка
            if (representationMode == RepresentationMode.NORMALIZED)
                getNormalizedMantissaAndExponent()
            round()
        } else
            throw IllegalInputFormatException("Input string has incorrect format: $input")
    }

    override fun toString(): String { //Перегружаем функцию преобразования объекта нашего класса в строку для вывода в консоль
        return listRepresentation.joinToString("")
    }

    private fun verifyInput(line: String): Boolean { //Метод проверки входной строки на соответствие формату дробного числа
        val regexPattern = Regex("^-?(\\d+|\\d+\\.\\d+)\$")
        val matches = regexPattern.find(line)
        return matches != null
    }

    private fun dropZeros() { //Метод для отбрасывания незначащих нулей дробной части числа
        if (dotIndex != -1) { //Если индекс разделительной
            val stringRepresentation = this.toString() //Преобразуем список с цифрами в строку
            val zerosRegex = Regex("\\.?(?<=\\.|\\d)0*\$") //Регулярка найдет незначащие нули
            val stringResult = zerosRegex.replace(stringRepresentation, "") //Заменяем незначащие нули пустыми символами
            listRepresentation =
                stringResult.toMutableList() //Преобразуем получившуюся строку в список и подменяем изначальный список
        } else return
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
            if (Config.roundingMode == RoundingMode.DROP) { //Режим отбрасывания
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

    //Перегружаем операторы +,-,*
    //TODO реализовать перегрузку операторов
    operator fun plus(other: HugeDecimal): HugeDecimal {
        return HugeDecimal("100.0")
    }

    operator fun minus(other: HugeDecimal): HugeDecimal {
        return HugeDecimal("200.0")
    }

    operator fun times(other: HugeDecimal): HugeDecimal {
        return HugeDecimal("300.0")
    }

    fun exponentialView(): String {
        var result = mantissa.joinToString("") + "*$base^$exponent"
        if (isNegative)
            result = "-$result"
        return result
    }

    companion object Config {
        private var mantissaLength: Int = 5 //Длина мантиссы
        private val base: Int = 10 //Основание системы счисления
        private var roundingMode: RoundingMode = RoundingMode.DROP //Режим округления
        private var representationMode =
            RepresentationMode.NORMALIZED //Способ представления числа (нормализованный или нет)

        fun setRoundingMode(roundingMode: RoundingMode) { //Метод установки режима округления
            Config.roundingMode = roundingMode
        }

        fun setRepresentationMode(representationMode: RepresentationMode) { //Метод установки способа представления числа
            Config.representationMode = representationMode
        }
    }
}