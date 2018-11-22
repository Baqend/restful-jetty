package info.orestes.rest.conversion;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by fbuec on 27.04.2016.
 */
public class MediaTypeNegotiation {
    public static final MediaType ALL = MediaType.parse("*/*");
    public static final List<MediaType> ANY = Arrays.asList(ALL);

    private static final MediaTypeRegistry TYPE_REGISTRY = MediaTypeRegistry.getDefaultRegistry();

    private static final Comparator<MediaType> QUALITY_COMPARATOR = (o1, o2) -> {
        float q1 = getQuality(o1);
        float q2 = getQuality(o2);

        if (q1 > q2) {
            return -1;
        } else if (q1 < q2) {
            return 1;
        } else if (!o1.getType().equals(o2.getType())) {
            if (o1.getType().equals("*")) {
                return 1;
            } else if (o2.getType().equals("*")) {
                return -1;
            } else {
                return o1.getType().compareTo(o2.getType());
            }
        } else if (!o1.getSubtype().equals(o2.getSubtype())) {
            if (o1.getSubtype().equals("*")) {
                return 1;
            } else if (o2.getSubtype().equals("*")) {
                return -1;
            } else {
                return o1.getSubtype().compareTo(o2.getSubtype());
            }
        } else {
            return 0;
        }
    };

    private static final Comparator<MediaType> INHERITANCE_COMPARATOR = (o1, o2) -> {
        MediaType m1 = o1.getBaseType();
        MediaType m2 = o2.getBaseType();

        if (TYPE_REGISTRY.isInstanceOf(m1, m2)) {
            //m1 is a specialisation of m2
            return -1;
        } else if (TYPE_REGISTRY.isInstanceOf(m2, m1)) {
            //m2 is a specialisation of m1
            return 1;
        } else {
            return 0;
        }
    };

    private static final Comparator<Entry<MediaType, Converter<?,?>>> ACCEPT_COMPARATOR =
        Comparator.<Entry<MediaType, Converter<?,?>>, Double>comparing(
            entry -> entry.getValue().getClass().getAnnotation(Accept.class).q(),
            Comparator.reverseOrder()
        ).thenComparing(
            Entry::getKey,
            inheritanceComparator()
        );

    public static Comparator<MediaType> qualityComparator() {
        return QUALITY_COMPARATOR;
    }

    public static Comparator<MediaType> inheritanceComparator() {
        return INHERITANCE_COMPARATOR;
    }

    public static Comparator<Entry<MediaType, Converter<?,?>>> acceptableComparator() {
        return ACCEPT_COMPARATOR;
    }

    public static float getQuality(MediaType mediaType) {
        String quality = mediaType.getParameters().get("q");

        if (quality != null) {
            try {
                float q = java.lang.Float.parseFloat(quality);
                if (q > 1.0)
                    return 1f;
                if (q < 0.0)
                    return 0f;
                return q;
            } catch (NumberFormatException ignored) { }
        }

        return 1f;
    }

    /**
     * Compare this {@link MediaType} with the given one if they are compatible.
     * {@link MediaType}s are compatible if they declare the same main and sub
     * type or one or both declare the main and or sub type as a wildcard or
     * they are compatible by mediatype {@link MediaTypeRegistry#isInstanceOf} inheritance
     *
     * @param subtype The {@link MediaType} the media type to check
     * @param superType The {@link MediaType} tha base or super type of the media type
     *
     * @return <code>true</code> if this {@link MediaType} is compatible to the
     *         given one, otherwise <code>false</code>
     */
    public static boolean isSubtypeOf(MediaType subtype, MediaType superType) {
        if (superType.getType().equals("*")) {
            return true;
        } else if (subtype.getType().equals(superType.getType())) {
            if (superType.getSubtype().equals("*")) {
                return true;
            }
        }

        return TYPE_REGISTRY.isInstanceOf(subtype.getBaseType(), superType.getBaseType());
    }

}
