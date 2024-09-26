package kp.rollingcube.ce.utils;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 *
 * @author Marc
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Version implements Comparable<Version>
{
    private static final Pattern PATTERN = Pattern.compile("^[0-9]+(.[0-9]+){0,3}$");
    
    public static final Version APP_VERSION = of(0, 1, 0);
    
    private int major;
    private int minor;
    private int patch;
    private int build;
    
    public static @NonNull Version of() { return new Version(); }
    public static @NonNull Version of(int major) { return new Version(major, 0, 0, 0); }
    public static @NonNull Version of(int major, int minor) { return new Version(major, minor, 0, 0); }
    public static @NonNull Version of(int major, int minor, int patch) { return new Version(major, minor, patch, 0); }
    public static @NonNull Version of(int major, int minor, int patch, int build) { return new Version(major, minor, patch, build); }
    
    public static @NonNull Version zero() { return of(); }
    
    public boolean isZero() { return major == 0 && minor == 0 && patch == 0 && build == 0; }
    
    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append(major).append('.').append(minor);
        if(patch > 0)
        {
            sb.append('.').append(patch);
            if (build > 0)
                sb.append('.').append(build);
        }
        else if(build > 0)
            sb.append(".0.").append(build);
        return sb.toString();
    }

    @Override
    public int compareTo(Version other)
    {
        if(other == null)
            return 1;
        
        int cmp = Integer.compare(major, other.major);
        if(cmp != 0) return cmp;

        cmp = Integer.compare(minor, other.minor);
        if(cmp != 0) return cmp;

        cmp = Integer.compare(patch, other.patch);
        if(cmp != 0) return cmp;

        return Integer.compare(build, other.build);
    }
}
