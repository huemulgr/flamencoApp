package ar.com.flamengo.huemul.flamengoapp.comun;

import java.util.Comparator;
import java.util.HashMap;

public class HashMapComparator implements Comparator {

    public int compare ( Object object1 , Object object2 ) {
        String value1 = ( String ) ( ( HashMap ) object1 ).get ( "ORDEN" );
        String value2 = ( String ) ( ( HashMap ) object2 ).get ( "ORDEN" );

        Integer obj1Value = Integer.valueOf(value1);
        Integer obj2Value = Integer.valueOf(value2) ;

        return obj1Value.compareTo ( obj2Value ) ;
    }
}
