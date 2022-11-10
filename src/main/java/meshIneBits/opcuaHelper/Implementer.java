package meshIneBits.opcuaHelper;

public class Implementer  {
    public static void main(String[] args){
Implementer i=new Implementer();
Cat cat=i.nextCat();
cat.meeeeow();

    }

    public Cat nextCat() {
        return ()-> {

                System.out.println("meeeeeeeeeow!!!!");
            return null;
        };

    }


}
