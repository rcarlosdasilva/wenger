package io.github.rcarlosdasilva.wenger.feature.region;

final class RegionConst {

  public static final String DEFAULT_PROVINCE_PATTERN = "^[1-9]\\d0000$";
  public static final String DEFAULT_CITY_PATTERN = "^[1-9]\\d{3}00$";
  public static final String DEFAULT_DISTRICT_PATTERN = "^\\d{4}[1-9]{2}$";
  public static final String PROVINCE_SUFFIX = "0000";
  public static final String CITY_SUFFIX = "00";

  private RegionConst() {
    throw new IllegalStateException();
  }

}
