import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static java.text.MessageFormat.format;

/**
 * Created by dave on 04/04/2017.
 */
public class Dimension {

    private String name;
    private String value;
    private String hierarchy;

    public Dimension(String[] row, DistinctDimensionsExtractor.Indices indices) {
        this.hierarchy = row[indices.hierarchyIndex()];
        this.name = row[indices.nameIndex()];
        this.value = row[indices.valueIndex()];
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }


    public String getHierarchy() {
        return hierarchy;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Dimension dimension = (Dimension) o;

        return new EqualsBuilder()
                .append(getName(), dimension.getName())
                .append(getValue(), dimension.getValue())
                .append(getHierarchy(), dimension.getHierarchy())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getValue())
                .append(getHierarchy())
                .toHashCode();
    }

    @Override
    public String toString() {
        return format("[hierarchy={0}, value={1}]", this.hierarchy, this.value);
    }
}
