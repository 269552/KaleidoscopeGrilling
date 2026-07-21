package cn.breezeth.kaleidoscope_grilling;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.world.item.ItemDisplayContext;

public final class SkewerItemRenderContext {
  private static final ThreadLocal<Deque<ItemDisplayContext>> CONTEXTS =
      ThreadLocal.withInitial(ArrayDeque::new);
  private static final ThreadLocal<Integer> CAPTURE_DEPTH = ThreadLocal.withInitial(() -> 0);

  public static void push(ItemDisplayContext context) {
    CONTEXTS.get().push(context);
  }

  public static void pop() {
    Deque<ItemDisplayContext> contexts = CONTEXTS.get();
    if (!contexts.isEmpty()) contexts.pop();
    if (contexts.isEmpty()) CONTEXTS.remove();
  }

  public static boolean isGui() {
    if (isCapturing()) return false;
    Deque<ItemDisplayContext> contexts = CONTEXTS.get();
    return !contexts.isEmpty() && contexts.peek() == ItemDisplayContext.GUI;
  }

  public static void pushCapture() {
    CAPTURE_DEPTH.set(CAPTURE_DEPTH.get() + 1);
  }

  public static void popCapture() {
    int depth = CAPTURE_DEPTH.get() - 1;
    if (depth <= 0) CAPTURE_DEPTH.remove();
    else CAPTURE_DEPTH.set(depth);
  }

  public static boolean isCapturing() {
    return CAPTURE_DEPTH.get() > 0;
  }

  private SkewerItemRenderContext() {}
}
