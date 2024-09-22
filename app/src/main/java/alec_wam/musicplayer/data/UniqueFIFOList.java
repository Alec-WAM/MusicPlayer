package alec_wam.musicplayer.data;

import java.util.ArrayList;

public class UniqueFIFOList<E> extends ArrayList<E> {
    private int maxSize;

    public UniqueFIFOList(int maxSize){
        super();
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(E item){
        this.remove(item);
        super.add(item);
        if(this.size() > this.maxSize){
            this.remove(0); //Remove last item
        }
        return true;
    }
}
