package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BigIntRangeMasking extends MaskingRuleDef {

  private static final Logger log = LoggerFactory.getLogger(BigIntRangeMasking.class);

  private static final String PARAM_STEP = "step";

  private static final String PARAM_STEP_DEFAULT = "10";

  public BigIntRangeMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public BigIntRangeMasking() {
  }

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskBigIntRange(config));
      log.info("Function added for Mask Type {}", this.getMaskType());
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
  }

  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName)
      throws RedmaskConfigException {

    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());

    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(config,
            MaskingConstants.MASK_BIGINT_RANGE_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding SQL query for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();
  }

  protected boolean validateAndAddParameters(List<String> parameters)
      throws RedmaskConfigException {

    for (String key : this.getMaskParams().keySet()) {
      if (!key.equals(PARAM_STEP)) {
        throw new RedmaskConfigException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }
    if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
      parameters.add(PARAM_STEP);
      return true;
    }
    int step = Integer.parseInt(this.getMaskParams().getOrDefault(PARAM_STEP, PARAM_STEP_DEFAULT));
    if (step > 0) {
      parameters.add(String.valueOf(step));
      return true;
    } else {
      throw new RedmaskConfigException(
          String.format("\'%s\' value should be greater than 0", PARAM_STEP));
    }
  }
}
