# Refinement prompts

Built-in image_gen edits to initial generated boards. Final selected files are 03-expenses-manual.png and 04-voice-flow.png.

## 03-expenses-manual
Reference/edit target: C:/Users/84966/.codex/generated_images/01a1296a-9cb1-74c3-b430-fe444d7338dd/exec-dfeafd8c-cc1f-4bcb-88b6-3ae65394e990.png

Edit this UI design board only for two corrections; preserve all other layout, colors, typography, Vietnamese wording and panels. 1) Change table footer in BOTH panels from 'Hiển thị 1 - 4 của 4 khoản chi' to 'Hiển thị 1 - 4 của 42 khoản chi'. The 4 rows are a page subset of monthly total 3.880.000 đ. 2) Remove the success toast 'Đã lưu khoản chi' underneath the open right modal entirely, restoring the dimmed background. The modal is still unsaved, so no success toast should appear. Preserve the open 'Thêm khoản chi' modal and all fields and buttons.

## 04-voice-flow
Reference/edit target: C:/Users/84966/.codex/generated_images/01a1296a-9cb1-74c3-b430-fe444d7338dd/exec-4533dff7-ddc9-494f-983c-d28abec70bc8.png

Edit ONLY the background summary cards of third/rightmost panel '3. Đã lưu' to reflect adding 50.000 đ after previous total3.880.000. Set right panel 'Tổng chi tiêu' to '3.930.000 đ'; set right panel 'So với tháng trước' to '-220.000 đ' with small 'Giảm 5,3%'. Previous month stays4.150.000 đ. Preserve EVERYTHING else, especially totals3.880.000 and decrease270.000 /6,5% in first two unsaved states, all foreground forms, typography, content, panel positions and colors. No extra features.
