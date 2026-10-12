package fr.geonature.maps.util

/**
 * This class implements a low-pass filter.
 *
 * A low-pass filter is an electronic filter that passes low-frequency signals but attenuates
 * (reduces the amplitude of) signals with frequencies higher than the cutoff frequency.
 * The actual amount of attenuation for each frequency varies from filter to filter.
 * It is sometimes called a high-cut filter, or treble cut filter when used in audio applications.
 *
 * @author S. Grimault
 */
object LowPassSensorValuesFilter {

    /**
     * time smoothing constant for low-pass filter
     * 0 <= alpha <= 1 ; a smaller value basically means more smoothing.
     *
     * @see [http://en.wikipedia.org/wiki/Low-pass_filter.Discrete-time_realization](http://en.wikipedia.org/wiki/Low-pass_filter.Discrete-time_realization)
     */
    private const val ALPHA = 0.25f

    /**
     * Filter the given input values against the previous values and return a low-pass filtered result.
     *
     * @param input float array to smooth.
     * @param output float array representing the previous values.
     *
     * @see [http://en.wikipedia.org/wiki/Low-pass_filter.Algorithmic_implementation](http://en.wikipedia.org/wiki/Low-pass_filter.Algorithmic_implementation)
     */
    fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) {
            return input
        }

        if (input.size != output.size) {
            throw IllegalArgumentException("input and output values must be the same length")
        }

        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }

        return output
    }
}
