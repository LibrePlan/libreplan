# Guava Upgrade Issue Analysis

## Problem Description

Attempted upgrade from Guava 19.0 to 33.5.0-android resulted in test failures:
- **Error**: `NoClassDefFoundError: Could not initialize class org.libreplan.web.common.ConfigurationModel`
- **Location**: `org.libreplan.web.planner.chart.ChartFillerTest`
- **Impact**: Webapp module tests failing due to class initialization failure

## Root Cause Analysis

The error occurs during static initialization of `ConfigurationModel` class:
```java
private static Map<String, String> currencies = getAllCurrencies();
```

This suggests that Guava 33.5.0-android introduced breaking changes that:
1. Affect static initializer execution
2. Cause class loading conflicts
3. Break compatibility with existing code

## Potential Fixes

### Option 1: Revert to Guava 19.0 (Recommended)
**Pros:**
- Maintains current working state
- No code changes required
- Zero risk

**Cons:**
- Does not address security vulnerabilities
- Stays on older version

**Implementation:**
```bash
# Already done - reverted in current main branch
git checkout HEAD -- pom.xml
```

### Option 2: Incremental Guava Upgrade
**Pros:**
- Addresses security vulnerabilities
- More manageable than huge jump

**Cons:**
- Requires testing at each step
- May still hit compatibility issues

**Implementation:**
```xml
<!-- Try intermediate versions first -->
<version>20.0</version>
<version>25.0-android</version>
<version>30.0</version>
```

### Option 3: Fix ConfigurationModel Initialization
**Pros:**
- Allows using latest Guava
- Targeted fix

**Cons:**
- Requires code analysis
- May uncover other issues

**Implementation:**
1. Debug static initializer in ConfigurationModel
2. Fix Guava-related initialization issues
3. Test thoroughly

### Option 4: Isolate Guava Usage
**Pros:**
- Minimizes impact
- Gradual migration

**Cons:**
- Complex refactoring
- Time-consuming

**Implementation:**
1. Identify all Guava usage
2. Replace with alternative libraries where possible
3. Test incrementally

## Recommendation

**Short-term**: Use Option 1 (Revert to 19.0)
- Maintains stability
- Allows time for proper upgrade planning

**Long-term**: Plan incremental upgrade (Option 2)
- Address security vulnerabilities
- Test at each version step
- Document compatibility issues

## Lessons Learned

1. **Thorough Testing Required**: Major version upgrades need comprehensive testing
2. **Incremental Approach**: Large version jumps risk breaking changes
3. **Static Initialization**: Be cautious with static field initialization
4. **Dependency Impact**: Upgrades affect more than just compilation

## Next Steps

1. Document current state (Guava 19.0)
2. Research Guava 19.0 → 20.0 upgrade
3. Plan incremental upgrade path
4. Schedule dedicated testing time
