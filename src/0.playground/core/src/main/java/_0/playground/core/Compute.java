package _0.playground.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.function.Function;

/**
 * 数学定数を指定された精度で演算するためのクラスです。
 */
public final class Compute {

	/**
	 * 1/2piを算定します。
	 *
	 * @param scale 精度
	 * @return 演算結果
	 */
	public static BigDecimal reciprocal_tau(final MathContext scale) {

		BigInteger n640320    = BigInteger.valueOf((int)Math.pow(2, 6) * 3 * 5 * 23 * 29);
		BigInteger n13591409  = BigInteger.valueOf(13 * 1045493);
		BigInteger n545140134 = BigInteger.valueOf(2 * (int)Math.pow(3, 2) * 7 * 11 * 19 * 127 * 163);

		BigInteger num2_factorial_cache = BigInteger.ONE;
		BigInteger div1_factorial_cache = BigInteger.ONE;
		BigInteger div2_factorial_cache = BigInteger.ONE;

		BigInteger n    = BigInteger.ZERO;
		BigDecimal prev = null;
		BigDecimal res  = BigDecimal.ZERO;
		while (true) {

			int n_ = n.intValueExact();
			if (0 < n.compareTo(BigInteger.ONE)) {
				num2_factorial_cache = num2_factorial_cache.multiply(factorial((n_ - 1) * 6 + 1, n_ * 6)); // (6n)!
				div1_factorial_cache = div1_factorial_cache.multiply(factorial((n_ - 1) * 3 + 1, n_ * 3)); // (3n)!
				div2_factorial_cache = div2_factorial_cache.multiply(n);                                   // n!
			}

			BigInteger num1 = BigInteger.ONE.negate().pow(n_);       // (-1)^n
			BigInteger num2 = num2_factorial_cache;                  // (6n)!
			BigInteger num3 = n13591409.add(n545140134.multiply(n)); // 13591409 + 545140134n
			BigInteger div1 = div1_factorial_cache;                  // (3n)!
			BigInteger div2 = div2_factorial_cache.pow(3);           // (n!)^3
			BigInteger div3 = n640320.pow(3 * n_);                   // 640320^(3n)

			BigDecimal num = new BigDecimal(BigInteger.ONE.multiply(num1).multiply(num2).multiply(num3));
			BigDecimal div = new BigDecimal(BigInteger.ONE.multiply(div1).multiply(div2).multiply(div3));

			res = res.add(num.divide(div, scale));

			BigDecimal res_ = res.setScale(scale.getPrecision(), scale.getRoundingMode());
			if (res_.equals(prev)) {
				break;
			}
			prev = res_;

			n = n.add(BigInteger.ONE);

		}
		res = BigDecimal.valueOf(6).divide(new BigDecimal(n640320.pow(3)).sqrt(scale), scale).multiply(res);

		return res;

	}

	/**
	 * 1/eを算定します。
	 *
	 * @param scale 精度
	 * @return 演算結果
	 */
	public static BigDecimal reciprocal_e(final MathContext scale) {

		BigInteger div_factorial_cache = BigInteger.ONE;

		BigInteger n    = BigInteger.ZERO;
		BigDecimal prev = null;
		BigDecimal res  = BigDecimal.ZERO;
		while (true) {

			if (0 < n.compareTo(BigInteger.ONE)) {
				div_factorial_cache = div_factorial_cache.multiply(n); // n!
			}

			BigDecimal num = BigDecimal.ONE;
			BigDecimal div = new BigDecimal(div_factorial_cache);

			res = res.add(num.divide(div, scale));

			BigDecimal res_ = res.setScale(scale.getPrecision(), scale.getRoundingMode());
			if (res_.equals(prev)) {
				break;
			}
			prev = res_;

			n = n.add(BigInteger.ONE);

		}

		return BigDecimal.ONE.divide(res, scale);

	}

	/**
	 * 1〜maxの乗算を演算します。
	 *
	 * @param max 最大値
	 * @return 演算結果
	 */
	public static BigInteger factorial(final int max) {
		return factorial(1, max);
	}

	/**
	 * min〜maxの乗算を演算します。
	 *
	 * @param min 最小値
	 * @param max 最大値
	 * @return 演算結果
	 */
	public static BigInteger factorial(final int min, final int max) {

		BigInteger res = BigInteger.ONE;

		for (int i = min; i <= max; i++) {
			res = res.multiply(BigInteger.valueOf(i));
		}

		return res;

	}

	/**
	 * vの分数乗を演算します。
	 *
	 * @param v     値
	 * @param n     分子
	 * @param d     分母
	 * @param scale 精度
	 * @return 演算結果
	 */
	public static BigDecimal pow(final BigDecimal v, final int n, final int d, final MathContext scale) {
		return sqrt(v.pow(n), d, scale);
	}

	/**
	 * vのn乗根を演算します。
	 *
	 * @param v     値
	 * @param n     根
	 * @param scale 精度
	 * @return 演算結果
	 */
	public static BigDecimal sqrt(final BigDecimal v, final int n, final MathContext scale) {

		Function<BigDecimal, BigDecimal> f  = x -> x.pow(n).subtract(v);
		Function<BigDecimal, BigDecimal> df = x -> BigDecimal.valueOf(n).multiply(x.pow(n - 1));

//		BigInteger n    = BigInteger.ZERO;
//		BigDecimal prev = null;
		BigDecimal res  = BigDecimal.ONE;
		for (int i = 0; i < 100; i++) {
			res = res.subtract(f.apply(res).divide(df.apply(res), scale));
		}

		return res;

	}

}
