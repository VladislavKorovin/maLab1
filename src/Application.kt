import java.math.BigDecimal

fun main(){
    val a = HugeDecimal("0")
    val b = HugeDecimal("0.00001234000")
    val c = HugeDecimal("2123124")
    val d = HugeDecimal("132.13948100")
    val e = HugeDecimal("-0.00002")
    print(a.exponentialView() + "\n")
    print(b.exponentialView()+ "\n")
    print(c.exponentialView()+ "\n")
    print(d.exponentialView()+ "\n")
    print(e.exponentialView()+ "\n")
}