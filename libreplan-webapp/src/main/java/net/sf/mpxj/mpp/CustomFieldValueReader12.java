/*
 * LibrePlan-local hotfix for a real bug in net.sf.mpxj:9.0.0.
 *
 * Placed under this exact package/class name on purpose: Java web-app class loaders load
 * WEB-INF/classes (this module's own compiled sources) before WEB-INF/lib/*.jar, so this
 * file shadows the upstream mpxj-9.0.0.jar class of the same name without needing to fork or
 * bump the whole library (mpxj is deliberately pinned at 9.0.0 - see pom.xml - since every
 * later release renames/removes API this project's own MPXJProjectFileConverter depends on).
 *
 * The bug: importing some older MPP12-format .mpp files (Project 2007/2010 binary format,
 * e.g. files originally saved by even older MS Project versions) throws
 * "NullPointerException: Cannot load from byte/boolean array because "data" is null" from
 * inside MPPUtility.getShort(), aborting the whole import.
 *
 * Root cause (confirmed by disassembling the actual mpxj-9.0.0.jar class, since no sources
 * jar is available offline): process() below reads two FixedData blocks per custom-field
 * outline-code entry. The first (m_outlineCodeFixedData) is already null-checked upstream.
 * The second (m_outlineCodeFixedData2) is NOT, even though it can legitimately be absent for
 * a given entry in older files (not corruption - MPPUtility.getGUID(), called on the very
 * same array, already tolerates this and quietly returns null for a null/too-short array;
 * only the sibling MPPUtility.getShort() call lacks that same guard). This class adds the
 * missing null check, mirroring the guard the first FixedData block already has, so a missing
 * entry just means that one custom-field value is registered without a GUID/type/lookup-table
 * link instead of crashing the entire file read. getTypedValue(null, value) (in the abstract
 * superclass) already handles a null type gracefully by falling back to a plain string value,
 * so this degrades cleanly rather than needing further special-casing here.
 *
 * Everything else in this file is an unmodified, behavior-preserving reconstruction of the
 * original process()/populateCustomFieldMap() methods.
 */
package net.sf.mpxj.mpp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.mpxj.CustomFieldContainer;
import net.sf.mpxj.CustomFieldLookupTable;
import net.sf.mpxj.CustomFieldValueDataType;
import net.sf.mpxj.FieldType;
import net.sf.mpxj.ProjectProperties;
import net.sf.mpxj.common.FieldTypeHelper;

public class CustomFieldValueReader12 extends CustomFieldValueReader
{
   public CustomFieldValueReader12(ProjectProperties properties, CustomFieldContainer container, VarMeta outlineCodeVarMeta,
         Var2Data outlineCodeVarData, FixedData outlineCodeFixedData, FixedData outlineCodeFixedData2, Props taskProps)
   {
      super(properties, container, outlineCodeVarMeta, outlineCodeVarData, outlineCodeFixedData, outlineCodeFixedData2, taskProps);
   }

   @Override public void process()
   {
      Integer[] uniqueid = m_outlineCodeVarMeta.getUniqueIdentifierArray();
      Map<UUID, FieldType> map = populateCustomFieldMap();

      for (int loop = 0; loop < uniqueid.length; loop++)
      {
         Integer id = uniqueid[loop];

         CustomFieldValueItem item = new CustomFieldValueItem(id);
         byte[] value = m_outlineCodeVarData.getByteArray(id, VALUE_LIST_VALUE);
         item.setDescription(m_outlineCodeVarData.getUnicodeString(id, VALUE_LIST_DESCRIPTION));
         item.setUnknown(m_outlineCodeVarData.getByteArray(id, VALUE_LIST_UNKNOWN));

         byte[] b = m_outlineCodeFixedData.getByteArrayValue(loop + 3);
         if (b != null)
         {
            item.setParent(Integer.valueOf(MPPUtility.getShort(b, 8)));
         }

         byte[] b2 = m_outlineCodeFixedData2.getByteArrayValue(loop + 3);
         UUID guid2 = null;

         if (b2 != null)
         {
            item.setGUID(MPPUtility.getGUID(b2, 0));
            guid2 = MPPUtility.getGUID(b2, 32);
            item.setType(CustomFieldValueDataType.getInstance(MPPUtility.getShort(b2, 48)));
         }

         item.setValue(getTypedValue(item.getType(), value));
         m_container.registerValue(item);

         FieldType field = map.get(guid2);
         if (field != null)
         {
            CustomFieldLookupTable table = m_container.getCustomField(field).getLookupTable();
            table.add(item);
            table.setGUID(guid2);
         }
      }
   }

   private Map<UUID, FieldType> populateCustomFieldMap()
   {
      byte[] data = m_taskProps.getByteArray(Props.CUSTOM_FIELDS);

      int offset = MPPUtility.getInt(data, 0) + 36;
      int count = MPPUtility.getInt(data, offset);
      offset += 4;
      offset += 8 * count;

      Map<UUID, FieldType> map = new HashMap<>();

      while (offset + 176 <= data.length)
      {
         int length = MPPUtility.getInt(data, offset);
         if (length <= 0 || offset + length > data.length)
         {
            break;
         }

         int fieldID = MPPUtility.getInt(data, offset + 4);
         FieldType field = FieldTypeHelper.getInstance(fieldID);
         UUID guid = MPPUtility.getGUID(data, offset + 160);
         map.put(guid, field);

         offset += length;
      }

      return map;
   }
}
