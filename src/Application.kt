fun main() {

    HugeDecimal.setMantissaLength(10)
    var a = HugeDecimal("19.91")
    var b = HugeDecimal("999.999")
    var j = HugeDecimal("-999")
    var d = HugeDecimal("-999.999")
    var e = HugeDecimal("-1000")
    var f = HugeDecimal("1")
    var g = HugeDecimal("-500")
    var h = HugeDecimal("100")

   // val c = a + b

    HugeDecimal.printAllNumbersForTask4()

}