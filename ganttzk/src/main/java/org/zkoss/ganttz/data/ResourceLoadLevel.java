package org.zkoss.ganttz.data;

/**
 * This enum handles the different load Levels assinged to ResourceLoad intervals . <br/>
 * @author Lorenzo Tilve Álvaro <ogonzalez@igalia.com>
 */
public enum ResourceLoadLevel {

    FULL_PLANIFICATED,
    PLANIFICATED,
    OVER_PLANIFICATED,
    NOT_PLANIFICATED;


    public static ResourceLoadLevel getFromPercentage(int percentage) {
        ResourceLoadLevel result = ResourceLoadLevel.NOT_PLANIFICATED;
        if ((percentage > 0) && (percentage < 100)) {
            result = ResourceLoadLevel.PLANIFICATED;
        } else if ( percentage==100 ) {
            result = ResourceLoadLevel.FULL_PLANIFICATED;
        } else if ( percentage > 100 ) {
            result = ResourceLoadLevel.OVER_PLANIFICATED;
        }
        return result;
    }

}
