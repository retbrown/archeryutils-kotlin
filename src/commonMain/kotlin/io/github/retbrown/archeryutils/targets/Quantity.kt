package io.github.retbrown.archeryutils.targets

/**
 * A quantity with units, e.g. a distance or a target diameter.
 *
 * It is recommended to use explicit units when specifying lengths in archeryutils for
 * readability, and to ensure the expected units are being used downstream.
 *
 * ```
 * val worcesterDistance = Quantity(80.0, "yard")
 * val worcesterTargetSize = Quantity(122.0, "cm")
 * ```
 *
 * @property value scalar value of the quantity.
 * @property units units of the quantity, e.g. `"cm"` or `"yard"`.
 */
public data class Quantity(val value: Double, val units: String) {
    /** Factory functions for common units. */
    public companion object {
        /** A quantity in centimetres. */
        public fun cm(value: Double): Quantity = Quantity(value, "cm")

        /** A quantity in metres. */
        public fun metres(value: Double): Quantity = Quantity(value, "metre")

        /** A quantity in yards. */
        public fun yards(value: Double): Quantity = Quantity(value, "yard")

        /** A quantity in inches. */
        public fun inches(value: Double): Quantity = Quantity(value, "inch")
    }
}
