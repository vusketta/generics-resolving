import com.gmail.vusketta.ContextFactoryKt;
import org.jetbrains.annotations.NotNull;

public class Main {
    public static void main(String[] args) {
        class A {
        }

        var a = new A();
        System.out.println(ContextFactoryKt.create(a.getClass()).getTypesMap());

        class B<T, V> extends A {
        }

        var b = new B<Long, Boolean>();
        System.out.println(ContextFactoryKt.create(b.getClass()).getTypesMap());

        class C extends B<String, Integer> {
        }

        var c = new C();
        System.out.println(ContextFactoryKt.create(c.getClass()).getTypesMap());

        var comparable = new Comparable<Integer>() {
            @Override
            public int compareTo(@NotNull Integer o) {
                return 0;
            }
        };
        System.out.println(ContextFactoryKt.create(comparable.getClass()).getTypesMap()); //unsupported anonymous classes
    }
}