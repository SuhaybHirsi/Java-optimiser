package comp207p.target;

public class ConstantVariableFolding
{
    public int methodOne(){
        int a = 62;
        int b = (a + 764) * 3;
        return b + 1234 - a;
    }

    public double methodTwo(){
        double i = 0.67;
        int j = 1;
        int x= 1-1;
        return i + j;
    }

    public boolean methodThree(){
        int x = 12345;
        int y = 54321;
        return x > y;
    }

    public boolean methodFour(){
        long x = 4835783423L;
        long y = 400000;
        long z = x + y;
        return x > y;
    }

    public int optimiseMe() {
        int a = 534245;
        int b = a - 1234;
        System.out.println((120298345-a)*38.435792873);
        for (int i = 0; i < 10; i++){
            System.out.println((b-a)*i);
        }
        return a*b;
    }

}