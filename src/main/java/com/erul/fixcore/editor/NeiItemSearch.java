package com.erul.fixcore.editor;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Use NEI's real query parser and its registered search providers, including NECH. */
public final class NeiItemSearch {
    private static boolean unavailable;
    private static Method getFilter;
    private static Method matches;
    private static String lastQuery;
    private static Object filter;

    private NeiItemSearch() {}

    public static synchronized boolean collect(Item item, String query, ArrayDeque<ItemStack> results) {
        if (unavailable) return false;
        try {
            if (getFilter == null) {
                matches = Class.forName("codechicken.nei.api.ItemFilter").getMethod("matches", ItemStack.class);
                getFilter = Class.forName("codechicken.nei.SearchField").getMethod("getFilter", String.class);
            }
            if (filter == null || !Objects.equals(query, lastQuery)) {
                filter = getFilter.invoke(null, query);
                lastQuery = query;
            }
            List<ItemStack> stacks = new ArrayList<>();
            item.func_150895_a(item, CreativeTabs.field_78027_g, stacks);
            if (stacks.isEmpty()) stacks.add(new ItemStack(item));
            for (ItemStack stack : stacks) {
                if ((Boolean) matches.invoke(filter, stack)) results.add(stack);
            }
            return true;
        } catch (ClassNotFoundException missing) {
            unavailable = true;
            return false;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("NEI search integration failed", failure);
        }
    }
}
