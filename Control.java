public class Control
{
    static Generator gen;

    String err = "";

    public Control(String text, int typ){

        /*
         * Unteren If abfragen sind dazu da um zu verhinden dass das Wort im Bereich ist
         */

        if(text.length() > 17){
            System.out.println("Du hast die maximale Textlänge überschritten. Die maximale Texlänge liegt bei 17");
            return;
        }else if(text.isEmpty()){
            System.out.println("Bitte gib einen Text ein, es können nicht keine Zeichen codiert werden.");
            return;
        }

        else{
            gen = new Generator(text, typ); // generator wird erstellt
        }
    }
}