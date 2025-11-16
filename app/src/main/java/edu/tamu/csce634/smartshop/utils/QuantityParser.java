package edu.tamu.csce634.smartshop.utils;

/**
 * 数量解析工具类
 * 用于解析数量字符串（如 "0.2 Oz"、"5 Oz"）并计算购买件数
 */
public class QuantityParser {

    /**
     * 解析后的数量结构
     */
    public static class ParsedQuantity {
        public final boolean success;    // 是否解析成功
        public final double value;       // 数值（如 0.2）
        public final String unit;        // 单位（如 "Oz"）

        public ParsedQuantity(boolean success, double value, String unit) {
            this.success = success;
            this.value = value;
            this.unit = unit;
        }

        // 解析失败时的默认值
        public static ParsedQuantity failed() {
            return new ParsedQuantity(false, 1.0, "");
        }
    }

    /**
     * 解析数量字符串
     * 支持格式：
     * - "2" -> {2.0, ""}
     * - "0.5 Oz" -> {0.5, "Oz"}
     * - "2.5 L" -> {2.5, "L"}
     *
     * @param quantityStr 数量字符串
     * @return 解析结果
     */
    public static ParsedQuantity parse(String quantityStr) {
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return ParsedQuantity.failed();
        }

        String str = quantityStr.trim();

        // 尝试纯数字解析
        try {
            double value = Double.parseDouble(str);
            return new ParsedQuantity(true, value, "");
        } catch (NumberFormatException e) {
            // 继续尝试 "数字 单位" 格式
        }

        // 尝试 "数字 单位" 格式（按第一个空格分割）
        int spaceIndex = str.indexOf(' ');
        if (spaceIndex > 0) {
            String numPart = str.substring(0, spaceIndex).trim();
            String unitPart = str.substring(spaceIndex + 1).trim();

            try {
                double value = Double.parseDouble(numPart);
                return new ParsedQuantity(true, value, unitPart);
            } catch (NumberFormatException e) {
                // 解析失败
            }
        }

        return ParsedQuantity.failed();
    }

    /**
     * 计算需要购买的件数
     *
     * @param neededValue 需要的数量（如 0.2）
     * @param packageSize 包装规格（如 5.0）
     * @return 需要购买的件数（向上取整）
     */
    public static int calculatePackageCount(double neededValue, double packageSize) {
        if (packageSize <= 0) {
            return 1; // 包装规格无效时，默认买1件
        }

        if (neededValue <= 0) {
            return 0; // 不需要时，买0件
        }

        // 向上取整：需要0.2，包装5，则买1件
        return (int) Math.ceil(neededValue / packageSize);
    }

    /**
     * 格式化数量显示（去除不必要的小数点）
     *
     * @param value 数值
     * @return 格式化字符串
     */
    public static String formatValue(double value) {
        // 如果是整数，不显示小数点
        if (Math.abs(value - Math.round(value)) < 1e-9) {
            return String.valueOf((int) Math.round(value));
        }
        // 否则保留最多2位小数
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}