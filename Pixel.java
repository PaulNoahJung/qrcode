public class Pixel
{
    boolean bit;
    int type;

    //types:
    // 1 = data
    // 2 = format information
    // 3 = Timing pattern
    // 4 = Alignment pattern
    // 5 = Seperator
    // 6 = ENCTYPE
    // 7 = MSG END
    
    public Pixel(boolean b, int t){
        bit = b;
        type = t;
    }

    // Getter und Setter Methoden

    public boolean getBit(){return bit;}

    public int getType(){return type;}

    public void setBit(boolean b){bit = b;}

    public void setType(int t){type = t;}

    public void flip(){ // True wird zu false bzw. False zu true
        if(bit == true){
            bit = false;
            return;
        }else if(bit == false){
            bit = true;
            return;
        }
    }
}
