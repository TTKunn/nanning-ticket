<template>
  <div>
    <div class="alert alert-warning" style="margin-bottom:12px;">
      <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8.982 1.566a1.13 1.13 0 00-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 01-1.1 0L7.1 5.995A.905.905 0 018 5zm.002 6a1 1 0 110 2 1 1 0 010-2z"/></svg>
      当前有 <strong>{{ warningCount }}</strong> 个库存记录剩余 ≤ 20，优先用「按区间调整」批量改日历库存。
    </div>

    <div class="stat-grid" style="grid-template-columns:repeat(4,1fr);margin-bottom:12px;">
      <div class="stat-card" v-for="item in summaryStats" :key="item.label">
        <div class="stat-card-header">
          <span class="stat-card-label">{{ item.label }}</span>
        </div>
        <div class="stat-card-value">{{ item.value }}</div>
        <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">{{ item.sub }}</div>
      </div>
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" v-model="filterKeyword" placeholder="搜索票种..." style="width:160px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenic">
            <option value="">全部园区</option>
            <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterTicket">
            <option value="">全部票种</option>
            <option v-for="t in ticketOptions" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </div>
        <div class="form-item">
          <input class="form-input" type="date" v-model="filterDateFrom" style="width:140px;" title="开始日期" />
        </div>
        <span style="color:var(--color-text-muted);">~</span>
        <div class="form-item">
          <input class="form-input" type="date" v-model="filterDateTo" style="width:140px;" title="结束日期" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="开放">开放</option>
            <option value="关闭">关闭</option>
            <option value="售罄">售罄</option>
          </select>
        </div>
        <button class="btn btn-default" @click="loadInventories">查询</button>
        <button class="btn btn-default" @click="resetFilter">重置</button>
        <div style="flex:1;"></div>
        <button class="btn btn-default" @click="openBatchAdjust">批量创建</button>
        <button class="btn btn-primary" @click="openRangeAdjust">按区间调整</button>
        <button class="btn btn-default" style="color:var(--color-danger);border-color:var(--color-danger);" @click="openRangeDelete">按区间删除</button>
      </div>
    </div>

    <!-- 多选操作浮动栏 -->
    <div v-if="selectedRows.length" class="card" style="margin-bottom:12px;background:var(--color-primary-light,#eef5ff);border-color:var(--color-primary,#3b82f6);">
      <div style="display:flex;align-items:center;gap:12px;">
        <span style="font-weight:600;">已选 {{ selectedRows.length }} 条</span>
        <span style="font-size:12px;color:var(--color-text-muted);">
          （已售 {{ selectedRows.reduce((s,r) => s + (r.sold||0), 0) }} 张）
        </span>
        <div style="flex:1;"></div>
        <button class="btn btn-default" @click="openSelectedAdjust('SET_TOTAL')">批量改总库存</button>
        <button class="btn btn-default" @click="openSelectedAdjust('INCREMENT')">+ / - 调整</button>
        <button class="btn btn-default" @click="openSelectedAdjust('SET_STATUS')">批量改状态</button>
        <button class="btn btn-default" @click="openSelectedAdjust('SET_REMARK')">批量加备注</button>
        <button class="btn btn-default" style="color:var(--color-danger);border-color:var(--color-danger);" @click="confirmSelectedDelete">批量删除</button>
        <button class="btn btn-default" @click="selectedRows = []">清空选择</button>
      </div>
    </div>

    <div class="card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th style="width:36px;">
                <input type="checkbox" :checked="isAllSelected" @change="toggleAll" />
              </th>
              <th>票种</th>
              <th>所属园区</th>
              <th>日期</th>
              <th>总库存</th>
              <th>已售</th>
              <th>剩余</th>
              <th>状态</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="10" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!inventoryList.length">
              <td colspan="10" class="empty-state">暂无库存数据</td>
            </tr>
            <tr v-for="item in inventoryList" v-else :key="item.id">
              <td>
                <input type="checkbox" :checked="isSelected(item)" @change="toggleOne(item)" />
              </td>
              <td>
                <div style="font-weight:600;">{{ item.ticketName }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ item.ticketCode }}</div>
              </td>
              <td>{{ item.scenicName }}</td>
              <td>{{ item.inventoryDate }}</td>
              <td>{{ item.total }}</td>
              <td>{{ item.sold || 0 }}</td>
              <td>
                <span :style="{ fontWeight:600, color: getStockColor(item.remaining) }">
                  {{ item.remaining }}
                </span>
              </td>
              <td>
                <span class="tag" :class="statusClass(item.status)">{{ item.status }}</span>
              </td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ item.remark || '—' }}</td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openAdjust(item)">调整</span>
                  <span class="action-link danger" @click="closeInventory(item)" v-if="item.status === '开放'">关闭</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条</span>
        <button class="page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadInventories()">«</button>
        <button v-for="p in pages" :key="p" class="page-btn" :class="{ active: p === pageNum }" @click="pageNum = p; loadInventories()">{{ p }}</button>
        <button class="page-btn" :disabled="pageNum >= pages" @click="pageNum++; loadInventories()">»</button>
      </div>
    </div>

    <!-- 调整日库存弹窗（单条） -->
    <div class="modal-mask" v-if="showAdjustModal" @click.self="showAdjustModal = false">
      <div class="modal-box" style="width:460px;">
        <div class="modal-header">
          <span class="modal-title">调整库存{{ selectedItem ? ' - ' + selectedItem.ticketName : '' }}</span>
          <button class="modal-close" @click="showAdjustModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">日期</label>
              <input class="form-input" type="date" v-model="form.inventoryDate" />
            </div>
            <div class="form-item">
              <label class="form-label">调整后总库存</label>
              <input class="form-input" type="number" v-model.number="form.total" placeholder="请输入调整数量" />
              <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;" v-if="selectedItem">
                当前已售 {{ selectedItem.sold || 0 }} 张，新总库存不能低于此值
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">备注</label>
              <input class="form-input" v-model="form.remark" placeholder="调整原因 / 说明" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showAdjustModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitAdjust">{{ saving ? '保存中...' : '保存调整' }}</button>
        </div>
      </div>
    </div>

    <!-- 批量创建库存弹窗（先选票种，再选区间） -->
    <div class="modal-mask" v-if="showBatchModal" @click.self="showBatchModal = false">
      <div class="modal-box" style="width:640px;">
        <div class="modal-header">
          <span class="modal-title">批量创建库存</span>
          <button class="modal-close" @click="showBatchModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <!-- 1. 先选票种 -->
            <div class="form-item">
              <label class="form-label">票种 <span style="color:var(--color-danger);">*</span></label>
              <select class="form-select" v-model="batchForm.ticketId">
                <option value="">请选择票种</option>
                <option v-for="t in ticketOptions" :key="t.id" :value="t.id">{{ t.name }}</option>
              </select>
              <div v-if="batchForm.ticketId && loadingExistingDates" style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">
                正在加载该票种已存在的库存日期...
              </div>
            </div>

            <!-- 2. 展示已存在日期 -->
            <div v-if="batchForm.ticketId && !loadingExistingDates" class="form-item">
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;">
                <label class="form-label" style="margin:0;">该票种已存在库存的日期（不可重复创建）</label>
                <span style="font-size:12px;color:var(--color-text-muted);">共 {{ existingDates.length }} 天</span>
                <button v-if="existingDates.length" type="button" class="btn btn-default" style="padding:2px 8px;font-size:12px;" @click="showAllExisting = !showAllExisting">
                  {{ showAllExisting ? '收起' : '展开全部' }}
                </button>
              </div>
              <div v-if="!existingDates.length" style="font-size:12px;color:var(--color-text-muted);padding:8px 12px;background:var(--color-bg-soft,#f6f8fa);border-radius:4px;">
                该票种还没有任何库存日期，可以放心创建。
              </div>
              <div v-else style="padding:8px 12px;background:var(--color-bg-soft,#f6f8fa);border-radius:4px;max-height:120px;overflow-y:auto;">
                <span v-for="(d, idx) in displayedExistingDates" :key="d" class="tag tag-gray" style="margin:2px 4px 2px 0;">
                  {{ d }}
                </span>
                <span v-if="!showAllExisting && existingDates.length > 30" style="font-size:12px;color:var(--color-text-muted);">... 还有 {{ existingDates.length - 30 }} 天</span>
              </div>
            </div>

            <!-- 3. 快捷区间按钮 -->
            <div v-if="batchForm.ticketId && !loadingExistingDates" class="form-item">
              <label class="form-label">快捷区间</label>
              <div style="display:flex;flex-wrap:wrap;gap:6px;">
                <button type="button" class="btn btn-default" style="padding:4px 10px;font-size:12px;" @click="applyQuickRange(7)">未来 7 天</button>
                <button type="button" class="btn btn-default" style="padding:4px 10px;font-size:12px;" @click="applyQuickRange(30)">未来 30 天</button>
                <button type="button" class="btn btn-default" style="padding:4px 10px;font-size:12px;" @click="applyQuickRange(60)">未来 60 天</button>
                <button type="button" class="btn btn-default" style="padding:4px 10px;font-size:12px;" @click="applyQuickRange(90)">未来 90 天</button>
                <button type="button" class="btn btn-default" style="padding:4px 10px;font-size:12px;" @click="applyQuickRange(180)">未来 180 天</button>
                <button type="button" class="btn btn-default" style="padding:4px 10px;font-size:12px;" @click="applyQuickRangeToYearEnd">到本年底</button>
                <button type="button" class="btn btn-default" style="padding:4px 10px;font-size:12px;color:var(--color-primary);" @click="applyRangeAfterLastExisting">从最后已有日期的次日</button>
              </div>
            </div>

            <!-- 4. 日期范围 -->
            <div v-if="batchForm.ticketId && !loadingExistingDates" style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">开始日期</label>
                <input class="form-input" type="date" v-model="batchForm.startDate" :min="todayStr" />
              </div>
              <div class="form-item">
                <label class="form-label">结束日期</label>
                <input class="form-input" type="date" v-model="batchForm.endDate" :min="batchForm.startDate || todayStr" />
              </div>
            </div>

            <!-- 5. 区间预览 -->
            <div v-if="batchForm.ticketId && batchForm.startDate && batchForm.endDate && !loadingExistingDates" class="form-item">
              <div :class="batchPreview.hasConflict ? 'alert alert-warning' : 'alert alert-info'" style="margin:0;padding:10px 12px;">
                <template v-if="batchPreview.totalDays > 0">
                  <div style="font-weight:600;margin-bottom:4px;">
                    区间预览：共 {{ batchPreview.totalDays }} 天
                    <span v-if="batchPreview.toCreate > 0" style="color:var(--color-green);">将创建 {{ batchPreview.toCreate }} 天</span>
                    <span v-if="batchPreview.toSkip > 0" style="color:var(--color-orange);">，跳过 {{ batchPreview.toSkip }} 天（已有库存）</span>
                  </div>
                  <div v-if="batchPreview.hasConflict && batchPreview.conflictDates.length" style="font-size:12px;margin-top:4px;">
                    冲突日期：{{ batchPreview.conflictDates.slice(0, 8).join('、') }}<span v-if="batchPreview.conflictDates.length > 8"> 等 {{ batchPreview.conflictDates.length }} 天</span>
                  </div>
                </template>
                <template v-else>
                  <div>日期范围无效，请检查起止日期。</div>
                </template>
              </div>
            </div>

            <!-- 6. 总库存 & 备注 -->
            <div v-if="batchForm.ticketId" class="form-item">
              <label class="form-label">每日总库存</label>
              <input class="form-input" type="number" v-model.number="batchForm.total" placeholder="如：500" />
            </div>
            <div v-if="batchForm.ticketId" class="form-item">
              <label class="form-label">备注</label>
              <input class="form-input" v-model="batchForm.remark" placeholder="可选" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showBatchModal = false">取消</button>
          <button
            class="btn btn-primary"
            :disabled="saving || !batchForm.ticketId || !batchForm.startDate || !batchForm.endDate || batchPreview.toCreate === 0"
            @click="submitBatch"
          >
            {{ saving ? '提交中...' : `批量创建${batchPreview.toCreate ? `（${batchPreview.toCreate} 天）` : ''}` }}
          </button>
        </div>
      </div>
    </div>

    <!-- 按区间调整弹窗（支持多种操作） -->
    <div class="modal-mask" v-if="showRangeAdjustModal" @click.self="showRangeAdjustModal = false">
      <div class="modal-box" style="width:560px;">
        <div class="modal-header">
          <span class="modal-title">按区间调整库存</span>
          <button class="modal-close" @click="showRangeAdjustModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">票种 <span style="color:var(--color-text-muted);font-weight:normal;">（不选则对所有票种）</span></label>
              <select class="form-select" v-model="rangeForm.ticketId">
                <option value="">所有票种</option>
                <option v-for="t in ticketOptions" :key="t.id" :value="t.id">{{ t.name }}</option>
              </select>
              <div v-if="rangeForm.ticketId && loadingExistingDates" style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">
                正在加载该票种已存在的库存日期...
              </div>
            </div>

            <!-- 已存在日期面板（仅在选了具体票种时显示） -->
            <div v-if="rangeForm.ticketId && !loadingExistingDates" class="form-item">
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;">
                <label class="form-label" style="margin:0;">该票种已存在库存的日期</label>
                <span style="font-size:12px;color:var(--color-text-muted);">共 {{ existingDates.length }} 天</span>
                <button v-if="existingDates.length" type="button" class="btn btn-default" style="padding:2px 8px;font-size:12px;" @click="showAllExisting = !showAllExisting">
                  {{ showAllExisting ? '收起' : '展开全部' }}
                </button>
              </div>
              <div v-if="!existingDates.length" style="font-size:12px;color:var(--color-text-muted);padding:8px 12px;background:var(--color-bg-soft,#f6f8fa);border-radius:4px;">
                该票种还没有任何库存日期。
              </div>
              <div v-else style="padding:8px 12px;background:var(--color-bg-soft,#f6f8fa);border-radius:4px;max-height:120px;overflow-y:auto;">
                <span v-for="d in displayedExistingDates" :key="d" class="tag tag-gray" style="margin:2px 4px 2px 0;">
                  {{ d }}
                </span>
                <span v-if="!showAllExisting && existingDates.length > 30" style="font-size:12px;color:var(--color-text-muted);">... 还有 {{ existingDates.length - 30 }} 天</span>
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">开始日期</label>
                <input class="form-input" type="date" v-model="rangeForm.dateFrom" />
              </div>
              <div class="form-item">
                <label class="form-label">结束日期</label>
                <input class="form-input" type="date" v-model="rangeForm.dateTo" />
              </div>
            </div>

            <!-- 命中预览：仅在选了具体票种 + 日期范围有效时显示 -->
            <div v-if="rangeForm.ticketId && rangeForm.dateFrom && rangeForm.dateTo && rangeForm.dateFrom <= rangeForm.dateTo" class="form-item">
              <div :class="rangeHitPreview.hit > 0 ? 'alert alert-info' : 'alert alert-warning'" style="margin:0;padding:10px 12px;">
                <template v-if="rangeHitPreview.totalDays > 0">
                  <div style="font-weight:600;margin-bottom:4px;">
                    区间预览：范围 {{ rangeHitPreview.totalDays }} 天
                    <span v-if="rangeHitPreview.hit > 0" style="color:var(--color-primary);">将命中 {{ rangeHitPreview.hit }} 条库存</span>
                    <span v-else>，无任何库存记录可操作</span>
                  </div>
                  <div v-if="rangeHitPreview.hit > 0 && rangeHitPreview.hitDates.length" style="font-size:12px;margin-top:4px;">
                    命中日期：{{ rangeHitPreview.hitDates.slice(0, 8).join('、') }}<span v-if="rangeHitPreview.hitDates.length > 8"> 等 {{ rangeHitPreview.hitDates.length }} 天</span>
                  </div>
                </template>
                <template v-else>
                  <div>日期范围无效，请检查起止日期。</div>
                </template>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">操作类型</label>
              <select class="form-select" v-model="rangeForm.operation" @change="onRangeOpChange">
                <option value="SET_TOTAL">设置总库存为指定值（覆盖）</option>
                <option value="INCREMENT">在现有库存上增加 N</option>
                <option value="DECREMENT">在现有库存上减少 N</option>
                <option value="SET_STATUS">批量开放 / 关闭</option>
                <option value="SET_REMARK">批量改备注</option>
              </select>
            </div>

            <div v-if="rangeForm.operation === 'SET_TOTAL'" class="form-item">
              <label class="form-label">目标总库存</label>
              <input class="form-input" type="number" v-model.number="rangeForm.total" placeholder="如：500" />
            </div>
            <div v-else-if="rangeForm.operation === 'INCREMENT' || rangeForm.operation === 'DECREMENT'" class="form-item">
              <label class="form-label">增减量 N <span style="color:var(--color-text-muted);font-weight:normal;">（正数）</span></label>
              <input class="form-input" type="number" v-model.number="rangeForm.delta" placeholder="如：100 或 -50" />
            </div>
            <div v-else-if="rangeForm.operation === 'SET_STATUS'" class="form-item">
              <label class="form-label">目标状态</label>
              <select class="form-select" v-model="rangeForm.status">
                <option value="开放">开放</option>
                <option value="关闭">关闭</option>
              </select>
            </div>
            <div v-else-if="rangeForm.operation === 'SET_REMARK'" class="form-item">
              <label class="form-label">备注</label>
              <input class="form-input" v-model="rangeForm.remark" placeholder="将覆盖原备注" />
            </div>

            <div class="form-item">
              <label style="display:flex;align-items:center;gap:6px;cursor:pointer;">
                <input type="checkbox" v-model="rangeForm.skipSold" />
                <span>跳过已售记录（强烈建议开启）</span>
              </label>
            </div>

            <div style="font-size:12px;color:var(--color-text-muted);background:var(--color-bg-soft,#f6f8fa);padding:8px 12px;border-radius:4px;">
              提示：操作完成后会在消息框中告知"成功 N 条 / 跳过 N 条"，被跳过的日期会列出原因。
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showRangeAdjustModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitRangeAdjust">{{ saving ? '提交中...' : '执行' }}</button>
        </div>
      </div>
    </div>

    <!-- 按日期删除弹窗（拖选日期网格） -->
    <div class="modal-mask" v-if="showRangeDeleteModal" @click.self="showRangeDeleteModal = false">
      <div class="modal-box" style="width:680px;">
        <div class="modal-header">
          <span class="modal-title" style="color:var(--color-danger);">按日期删除库存</span>
          <button class="modal-close" @click="showRangeDeleteModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">票种 <span style="color:var(--color-text-muted);font-weight:normal;">（不选则对所有票种）</span></label>
              <select class="form-select" v-model="rangeDeleteForm.ticketId">
                <option value="">所有票种</option>
                <option v-for="t in ticketOptions" :key="t.id" :value="t.id">{{ t.name }}</option>
              </select>
              <div v-if="rangeDeleteForm.ticketId && loadingExistingDates" style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">
                正在加载该票种已存在的库存日期...
              </div>
            </div>

            <!-- 拖选日期网格（仅在选了具体票种时显示） -->
            <div v-if="rangeDeleteForm.ticketId && !loadingExistingDates" class="form-item">
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:8px;flex-wrap:wrap;">
                <label class="form-label" style="margin:0;">点击 / 拖选要删除的日期</label>
                <span style="font-size:12px;color:var(--color-text-muted);">
                  已选 <strong style="color:var(--color-danger);">{{ selectedDates.size }}</strong> / 共 {{ existingDates.length }} 天
                </span>
                <div style="flex:1;"></div>
                <button type="button" class="btn btn-default" style="padding:2px 10px;font-size:12px;" @click="selectAllDates">全选</button>
                <button type="button" class="btn btn-default" style="padding:2px 10px;font-size:12px;" @click="invertDateSelection">反选</button>
                <button type="button" class="btn btn-default" style="padding:2px 10px;font-size:12px;" @click="clearDateSelection">清空</button>
              </div>

              <div v-if="!existingDates.length" style="font-size:12px;color:var(--color-text-muted);padding:16px;background:var(--color-bg-soft,#f6f8fa);border-radius:4px;text-align:center;">
                该票种还没有任何库存日期，没有可删除的内容。
              </div>
              <div
                v-else
                style="display:flex;flex-wrap:wrap;gap:6px;max-height:240px;overflow-y:auto;padding:10px;background:var(--color-bg-soft,#f6f8fa);border-radius:4px;user-select:none;"
                @selectstart.prevent
              >
                <button
                  v-for="d in existingDates"
                  :key="d"
                  type="button"
                  :title="d"
                  :class="selectedDates.has(d) ? 'date-btn is-selected' : 'date-btn'"
                  @mousedown.prevent="startDragDate(d)"
                  @mouseenter="onDragEnterDate(d)"
                >{{ d.slice(5) }}</button>
              </div>
              <div v-if="existingDates.length" style="font-size:12px;color:var(--color-text-muted);margin-top:6px;">
                💡 单击切换 · 按住左键在按钮上滑动可批量选 / 取消
              </div>
            </div>

            <!-- 「所有票种」模式提示 -->
            <div v-if="!rangeDeleteForm.ticketId" class="form-item">
              <div class="alert alert-info" style="margin:0;padding:10px 12px;">
                「所有票种」模式请使用列表页的多选删除功能，此处不支持。
              </div>
            </div>

            <div v-if="rangeDeleteForm.ticketId && existingDates.length" class="form-item">
              <label style="display:flex;align-items:center;gap:6px;cursor:pointer;">
                <input type="checkbox" v-model="rangeDeleteForm.onlyUnsold" />
                <span>仅删除未售记录（推荐）</span>
              </label>
              <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">
                取消勾选后会强制删除包括已售记录，<strong>可能影响已出票的核销</strong>，请谨慎。
              </div>
            </div>

            <div v-if="rangeDeleteForm.ticketId && selectedDates.size > 0" class="form-item">
              <label class="form-label">确认输入 <span style="color:var(--color-danger);">*</span></label>
              <input class="form-input" v-model="rangeDeleteForm.confirmText" placeholder='输入 "确认删除" 以启用按钮' />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showRangeDeleteModal = false">取消</button>
          <button
            class="btn btn-primary"
            :disabled="saving || !rangeDeleteForm.ticketId || selectedDates.size === 0 || rangeDeleteForm.confirmText !== '确认删除'"
            style="background:var(--color-danger,#ef4444);border-color:var(--color-danger,#ef4444);"
            @click="submitRangeDelete"
          >{{ saving ? '删除中...' : `删除 ${selectedDates.size} 天` }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import {
  listInventories, createInventory, batchCreateInventory,
  batchUpdateInventory, batchDeleteInventory, toggleInventoryStatus,
  listInventoryDates,
} from '../api/inventory'
import { listScenicOptions } from '../api/scenic'
import { listTicketOptions } from '../api/ticket'
import { watch } from 'vue'

const inventoryList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const pages = ref(1)
const loading = ref(false)
const saving = ref(false)

const scenics = ref([])
const ticketOptions = ref([])

const filterKeyword = ref('')
const filterScenic = ref('')
const filterTicket = ref('')
const filterDateFrom = ref('')
const filterDateTo = ref('')
const filterStatus = ref('')

const summaryStats = ref([
  { label: '库存记录', value: '0', sub: '当前查询结果' },
  { label: '总库存余额', value: '0', sub: '跨记录汇总剩余总量' },
  { label: '已售出', value: '0', sub: '当前查询范围内' },
  { label: '预警记录', value: '0', sub: '剩余 ≤ 20' },
])

const showAdjustModal = ref(false)
const showBatchModal = ref(false)
const showRangeAdjustModal = ref(false)
const showRangeDeleteModal = ref(false)
const selectedItem = ref(null)
const selectedRows = ref([])

// 批量创建弹窗专用状态
const existingDates = ref([])          // 当前选中票种已存在的全部日期（ISO yyyy-mm-dd）
const loadingExistingDates = ref(false)
const showAllExisting = ref(false)     // 是否展开"已存在日期"全部

// 按区间删除：拖选日期网格专用状态
const selectedDates = reactive(new Set())  // 已选日期（reactive Set 让 add/delete 自动触发渲染）
const isDragging = ref(false)
const dragMode = ref('add')                // 'add' | 'remove'：按下时决定的拖选模式

const form = reactive({ inventoryDate: '', total: 0, remark: '' })
const batchForm = reactive({
  ticketId: '',
  startDate: '',
  endDate: '',
  total: 0,
  remark: '',
})
const rangeForm = reactive({
  ticketId: '',
  dateFrom: '',
  dateTo: '',
  operation: 'SET_TOTAL',
  total: 0,
  delta: 0,
  status: '开放',
  remark: '',
  skipSold: true,
})
const rangeDeleteForm = reactive({
  ticketId: '',
  onlyUnsold: true,
  confirmText: '',
})

const warningCount = computed(() => inventoryList.value.filter(it => Number(it.remaining || 0) <= 20).length)

// 今天（yyyy-MM-dd），用于限制日期选择器最小日期
const todayStr = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
})

// 已存在日期展示：默认只展示前 30 个
const displayedExistingDates = computed(() => {
  return showAllExisting.value ? existingDates.value : existingDates.value.slice(0, 30)
})

// 把 ISO 日期字符串安全转 Date
function isoToDate(iso) {
  if (!iso) return null
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}
function dateToIso(date) {
  if (!date) return ''
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
function diffDays(fromIso, toIso) {
  const a = isoToDate(fromIso)
  const b = isoToDate(toIso)
  if (!a || !b) return 0
  return Math.round((b - a) / 86400000) + 1
}

// 区间预览：总天数 / 将创建 / 跳过 / 冲突日期
const batchPreview = computed(() => {
  const { startDate, endDate } = batchForm
  if (!startDate || !endDate || startDate > endDate) {
    return { totalDays: 0, toCreate: 0, toSkip: 0, hasConflict: false, conflictDates: [] }
  }
  const existSet = new Set(existingDates.value)
  const total = diffDays(startDate, endDate)
  const conflict = []
  const start = isoToDate(startDate)
  const cursor = new Date(start)
  for (let i = 0; i < total; i++) {
    const iso = dateToIso(cursor)
    if (existSet.has(iso)) conflict.push(iso)
    cursor.setDate(cursor.getDate() + 1)
  }
  return {
    totalDays: total,
    toCreate: total - conflict.length,
    toSkip: conflict.length,
    hasConflict: conflict.length > 0,
    conflictDates: conflict,
  }
})

// 监听票种变化：拉取已存在日期
watch(() => batchForm.ticketId, async (newId) => {
  existingDates.value = []
  if (!newId) return
  loadingExistingDates.value = true
  try {
    const list = await listInventoryDates(newId)
    // 兼容后端返回 Date / LocalDateTime / "yyyy-MM-dd" 字符串
    existingDates.value = (list || []).map(d => {
      if (typeof d === 'string') return d.slice(0, 10)
      const dt = new Date(d)
      return dateToIso(dt)
    }).filter(Boolean).sort()
  } catch (e) { /* handled */ }
  finally { loadingExistingDates.value = false }
})

// 监听按区间调整的票种：复用同一份 existingDates
// 选了具体票种才拉；选了「所有票种」则清空（不显示已存在日期面板）
watch(() => rangeForm.ticketId, async (newId) => {
  if (!newId) {
    existingDates.value = []
    return
  }
  loadingExistingDates.value = true
  try {
    const list = await listInventoryDates(newId)
    existingDates.value = (list || []).map(d => {
      if (typeof d === 'string') return d.slice(0, 10)
      const dt = new Date(d)
      return dateToIso(dt)
    }).filter(Boolean).sort()
  } catch (e) { /* handled */ }
  finally { loadingExistingDates.value = false }
})

// 监听按区间删除的票种：复用同一份 existingDates
watch(() => rangeDeleteForm.ticketId, async (newId) => {
  selectedDates.clear()  // 切票种先清空选择，避免误删
  if (!newId) {
    existingDates.value = []
    return
  }
  loadingExistingDates.value = true
  try {
    const list = await listInventoryDates(newId)
    existingDates.value = (list || []).map(d => {
      if (typeof d === 'string') return d.slice(0, 10)
      const dt = new Date(d)
      return dateToIso(dt)
    }).filter(Boolean).sort()
  } catch (e) { /* handled */ }
  finally { loadingExistingDates.value = false }
})

// 按区间调整的命中预览：统计 [dateFrom, dateTo] 内属于 existingDates 的天数
const rangeHitPreview = computed(() => {
  const { ticketId, dateFrom, dateTo } = rangeForm
  if (!ticketId || !dateFrom || !dateTo || dateFrom > dateTo) {
    return { totalDays: 0, hit: 0, hasHit: false, hitDates: [] }
  }
  const existSet = new Set(existingDates.value)
  const totalDays = diffDays(dateFrom, dateTo)
  const start = isoToDate(dateFrom)
  const cursor = new Date(start)
  const hit = []
  for (let i = 0; i < totalDays; i++) {
    const iso = dateToIso(cursor)
    if (existSet.has(iso)) hit.push(iso)
    cursor.setDate(cursor.getDate() + 1)
  }
  return { totalDays, hit: hit.length, hasHit: hit.length > 0, hitDates: hit }
})

/* ====================== 按日期删除：拖选日期网格 ====================== */

// 按下按钮：开始拖选；如果按下的日期已选中，则本次拖选为「取消」模式，反之为「选中」模式
function startDragDate(date) {
  isDragging.value = true
  if (selectedDates.has(date)) {
    dragMode.value = 'remove'
    selectedDates.delete(date)
  } else {
    dragMode.value = 'add'
    selectedDates.add(date)
  }
}
// 拖动过程中滑过其他日期：按当前模式 add / remove
function onDragEnterDate(date) {
  if (!isDragging.value) return
  if (dragMode.value === 'add') selectedDates.add(date)
  else selectedDates.delete(date)
}
// 任意位置松开鼠标：结束拖选
function endDragDate() {
  isDragging.value = false
}
// 全选 / 反选 / 清空
function selectAllDates() {
  for (const d of existingDates.value) selectedDates.add(d)
}
function invertDateSelection() {
  const keep = new Set()
  for (const d of existingDates.value) {
    if (!selectedDates.has(d)) keep.add(d)
  }
  selectedDates.clear()
  for (const d of keep) selectedDates.add(d)
}
function clearDateSelection() {
  selectedDates.clear()
}

// 打开批量创建弹窗：清空状态
function openBatchAdjust() {
  Object.assign(batchForm, {
    ticketId: filterTicket.value || '',
    startDate: filterDateFrom.value || todayStr.value,
    endDate: filterDateTo.value || todayStr.value,
    total: 500, remark: '',
  })
  existingDates.value = []
  showAllExisting.value = false
  showBatchModal.value = true
}

// 快捷区间：今天起 N 天
function applyQuickRange(days) {
  const start = new Date()
  const end = new Date()
  end.setDate(end.getDate() + days - 1)
  batchForm.startDate = dateToIso(start)
  batchForm.endDate = dateToIso(end)
}

// 快捷区间：到本年 12-31
function applyQuickRangeToYearEnd() {
  const start = new Date()
  const end = new Date(start.getFullYear(), 11, 31)
  if (end < start) {
    // 本年已过，跳到次年底
    end.setFullYear(end.getFullYear() + 1)
  }
  batchForm.startDate = dateToIso(start)
  batchForm.endDate = dateToIso(end)
}

// 快捷区间：从最后已有日期的次日开始，未来 90 天
function applyRangeAfterLastExisting() {
  if (!existingDates.value.length) {
    applyQuickRange(90)
    return
  }
  const last = isoToDate(existingDates.value[existingDates.value.length - 1])
  if (!last) {
    applyQuickRange(90)
    return
  }
  const start = new Date(last)
  start.setDate(start.getDate() + 1)
  // 如果开始日期早于今天，挪到今天
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (start < today) start.setTime(today.getTime())
  const end = new Date(start)
  end.setDate(end.getDate() + 89)
  batchForm.startDate = dateToIso(start)
  batchForm.endDate = dateToIso(end)
}

function getStockColor(remaining) {
  if (!remaining || remaining === 0) return 'var(--color-gray-400)'
  if (remaining < 20) return 'var(--color-red)'
  if (remaining < 50) return 'var(--color-orange)'
  return 'var(--color-text-primary)'
}

function statusClass(s) {
  return { 开放: 'tag-green', 关闭: 'tag-gray', 售罄: 'tag-red' }[s] || 'tag-gray'
}

// 按区间调整的操作类型 → 中文（用于警告信息）
const OPERATION_LABELS = {
  SET_TOTAL: '设置总库存',
  INCREMENT: '增加库存',
  DECREMENT: '减少库存',
  SET_STATUS: '修改状态',
  SET_REMARK: '修改备注',
}
function operationLabel(op) {
  return OPERATION_LABELS[op] || '该操作'
}

function isSelected(item) {
  return selectedRows.value.some(r => r.id === item.id)
}

const isAllSelected = computed(() => {
  return inventoryList.value.length > 0 && selectedRows.value.length === inventoryList.value.length
})

function toggleOne(item) {
  const idx = selectedRows.value.findIndex(r => r.id === item.id)
  if (idx >= 0) selectedRows.value.splice(idx, 1)
  else selectedRows.value.push(item)
}

function toggleAll(e) {
  if (e.target.checked) {
    // 用 id 去重追加
    const existing = new Set(selectedRows.value.map(r => r.id))
    inventoryList.value.forEach(it => { if (!existing.has(it.id)) selectedRows.value.push(it) })
  } else {
    // 只清掉当前页的（避免误清掉翻页前已选）
    const currentIds = new Set(inventoryList.value.map(r => r.id))
    selectedRows.value = selectedRows.value.filter(r => !currentIds.has(r.id))
  }
}

async function loadScenics() {
  try { scenics.value = await listScenicOptions() } catch (e) { /* handled */ }
}
async function loadTicketOptions() {
  try { ticketOptions.value = await listTicketOptions() } catch (e) { /* handled */ }
}

async function loadInventories() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterScenic.value) params.scenicId = filterScenic.value
    if (filterTicket.value) params.ticketId = filterTicket.value
    if (filterDateFrom.value) params.dateFrom = filterDateFrom.value
    if (filterDateTo.value) params.dateTo = filterDateTo.value
    if (filterStatus.value) params.status = filterStatus.value
    const data = await listInventories(params)
    inventoryList.value = data?.records || []
    total.value = data?.total || 0
    pages.value = data?.pages || 1

    // 汇总
    let totalStock = 0, totalSold = 0, totalRemain = 0
    inventoryList.value.forEach(it => {
      totalStock += Number(it.total || 0)
      totalSold += Number(it.sold || 0)
      totalRemain += Number(it.remaining || 0)
    })
    summaryStats.value[0].value = String(total.value)
    summaryStats.value[0].sub = `共 ${inventoryList.value.length} 条（本页）`
    summaryStats.value[1].value = totalRemain.toLocaleString()
    summaryStats.value[1].sub = `总库存 ${totalStock.toLocaleString()}`
    summaryStats.value[2].value = totalSold.toLocaleString()
    summaryStats.value[3].value = String(warningCount.value)
    summaryStats.value[3].sub = filterDateFrom.value || filterDateTo.value ? '按当前筛选统计' : '按全部记录统计'
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function resetFilter() {
  filterKeyword.value = ''
  filterScenic.value = ''
  filterTicket.value = ''
  filterDateFrom.value = ''
  filterDateTo.value = ''
  filterStatus.value = ''
  pageNum.value = 1
  loadInventories()
}

function openAdjust(item) {
  selectedItem.value = item
  Object.assign(form, {
    inventoryDate: item.inventoryDate,
    total: item.total,
    remark: item.remark || '',
  })
  showAdjustModal.value = true
}

function openRangeAdjust() {
  Object.assign(rangeForm, {
    ticketId: filterTicket.value || '',
    dateFrom: filterDateFrom.value || new Date().toISOString().slice(0, 10),
    dateTo: filterDateTo.value || new Date().toISOString().slice(0, 10),
    operation: 'SET_TOTAL',
    total: 500,
    delta: 0,
    status: '开放',
    remark: '',
    skipSold: true,
  })
  showRangeAdjustModal.value = true
}

function openRangeDelete() {
  Object.assign(rangeDeleteForm, {
    ticketId: filterTicket.value || '',
    onlyUnsold: true,
    confirmText: '',
  })
  selectedDates.clear()  // 打开弹窗也清空，避免上次残留
  showRangeDeleteModal.value = true
}

function onRangeOpChange() {
  // 切操作类型时清空与该操作无关的字段
  rangeForm.total = 0
  rangeForm.delta = 0
  rangeForm.status = '开放'
  rangeForm.remark = ''
}

function openSelectedAdjust(operation) {
  if (!selectedRows.value.length) {
    ElMessage({ type: 'warning', message: '请先选择要操作的记录' })
    return
  }
  // 用已选行的 ticketId / 日期 范围 回填表单
  const ticketIds = new Set(selectedRows.value.map(r => r.ticketId))
  const ticketId = ticketIds.size === 1 ? [...ticketIds][0] : ''
  const dates = selectedRows.value.map(r => r.inventoryDate).filter(Boolean).sort()
  Object.assign(rangeForm, {
    ticketId: ticketId || '',
    dateFrom: dates[0] || '',
    dateTo: dates[dates.length - 1] || '',
    operation,
    skipSold: true,
  })
  if (operation === 'SET_TOTAL') rangeForm.total = 500
  if (operation === 'INCREMENT' || operation === 'DECREMENT') rangeForm.delta = 50
  if (operation === 'SET_STATUS') rangeForm.status = '开放'
  if (operation === 'SET_REMARK') rangeForm.remark = ''
  showRangeAdjustModal.value = true
}

async function confirmSelectedDelete() {
  if (!selectedRows.value.length) return
  if (selectedRows.value.some(r => (r.sold || 0) > 0)) {
    ElMessage({ type: 'warning', message: '已选项中包含已售记录，请改用"按区间删除"并取消勾选"仅删未售"' })
    return
  }
  if (!window.confirm(`确认删除已选 ${selectedRows.value.length} 条库存记录？此操作不可撤销`)) return
  saving.value = true
  try {
    // 按 ticketId 分组调用批量删除
    const groups = new Map()
    selectedRows.value.forEach(r => {
      const k = r.ticketId
      if (!groups.has(k)) groups.set(k, [])
      groups.get(k).push(r.inventoryDate)
    })
    let totalSuccess = 0, totalSkip = 0
    for (const [tid, dateList] of groups) {
      const res = await batchDeleteInventory({
        ticketId: tid, dates: dateList, onlyUnsold: true,
      })
      totalSuccess += res?.successCount || 0
      totalSkip += res?.skipCount || 0
    }
    ElMessage({ type: 'success', message: `删除完成：成功 ${totalSuccess} 条${totalSkip ? `，跳过 ${totalSkip} 条` : ''}` })
    selectedRows.value = []
    loadInventories()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function submitAdjust() {
  if (!selectedItem.value) return
  saving.value = true
  try {
    await createInventory({
      ticketId: selectedItem.value.ticketId,
      inventoryDate: form.inventoryDate,
      total: form.total,
      status: '开放',
      remark: form.remark,
    })
    ElMessage({ type: 'success', message: '库存已更新' })
    showAdjustModal.value = false
    loadInventories()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function submitBatch() {
  if (!batchForm.ticketId) { ElMessage({ type: 'warning', message: '请选择票种' }); return }
  if (!batchForm.startDate || !batchForm.endDate) { ElMessage({ type: 'warning', message: '请选择日期范围' }); return }
  if (batchForm.startDate > batchForm.endDate) { ElMessage({ type: 'warning', message: '开始日期不能晚于结束日期' }); return }
  const preview = batchPreview.value
  if (preview.totalDays <= 0) { ElMessage({ type: 'warning', message: '日期范围无效' }); return }
  if (preview.toCreate === 0) {
    ElMessage({ type: 'warning', message: `所选区间 ${preview.totalDays} 天全部已存在库存，没有可创建的新日期` })
    return
  }
  // 有冲突时多一次确认
  if (preview.toSkip > 0) {
    const ok = window.confirm(
      `所选区间共 ${preview.totalDays} 天，其中 ${preview.toCreate} 天将创建，${preview.toSkip} 天已存在将被跳过。\n\n是否继续？`
    )
    if (!ok) return
  }
  saving.value = true
  try {
    const res = await batchCreateInventory(batchForm)
    const created = typeof res === 'number' ? res : preview.toCreate
    ElMessage({
      type: 'success',
      message: `批量创建完成：新增 ${created} 天${preview.toSkip ? `，跳过 ${preview.toSkip} 天已存在` : ''}`,
    })
    showBatchModal.value = false
    loadInventories()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

function buildRangeAdjustPayload() {
  const payload = {
    ticketId: rangeForm.ticketId || null,
    dateFrom: rangeForm.dateFrom || null,
    dateTo: rangeForm.dateTo || null,
    operation: rangeForm.operation,
    skipSold: rangeForm.skipSold,
  }
  if (rangeForm.operation === 'SET_TOTAL') payload.total = rangeForm.total
  else if (rangeForm.operation === 'INCREMENT' || rangeForm.operation === 'DECREMENT') payload.delta = rangeForm.delta
  else if (rangeForm.operation === 'SET_STATUS') payload.status = rangeForm.status
  else if (rangeForm.operation === 'SET_REMARK') payload.remark = rangeForm.remark
  return payload
}

function summarizeBatchResult(res) {
  if (!res) return '完成'
  const { successCount = 0, skipCount = 0, skipped } = res
  let msg = `成功 ${successCount} 条${skipCount ? `，跳过 ${skipCount} 条` : ''}`
  if (skipped && Object.keys(skipped).length) {
    const sample = Object.entries(skipped).slice(0, 3)
      .map(([d, r]) => `${d}: ${r}`).join('；')
    const more = Object.keys(skipped).length > 3 ? '…' : ''
    msg += `（${sample}${more}）`
  }
  return msg
}

async function submitRangeAdjust() {
  // 基础校验
  if (!rangeForm.dateFrom || !rangeForm.dateTo) {
    ElMessage({ type: 'warning', message: '请选择日期范围' }); return
  }
  if (rangeForm.dateFrom > rangeForm.dateTo) {
    ElMessage({ type: 'warning', message: '开始日期不能晚于结束日期' }); return
  }
  // 选了具体票种时，若区间内 0 条可命中，给出友好提示并阻止
  if (rangeForm.ticketId) {
    const hit = rangeHitPreview.value.hit
    if (hit === 0) {
      ElMessage({
        type: 'warning',
        message: `所选区间 ${rangeHitPreview.value.totalDays} 天内没有任何库存记录，无法执行${operationLabel(rangeForm.operation)}`,
      })
      return
    }
  }
  if (rangeForm.operation === 'SET_TOTAL' && (rangeForm.total == null || rangeForm.total < 0)) {
    ElMessage({ type: 'warning', message: '请填写总库存' }); return
  }
  if ((rangeForm.operation === 'INCREMENT' || rangeForm.operation === 'DECREMENT')
      && (rangeForm.delta == null || rangeForm.delta === 0)) {
    ElMessage({ type: 'warning', message: '请填写增减量（正数）' }); return
  }
  saving.value = true
  try {
    const res = await batchUpdateInventory(buildRangeAdjustPayload())
    ElMessage({ type: 'success', message: summarizeBatchResult(res) })
    showRangeAdjustModal.value = false
    selectedRows.value = []
    loadInventories()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function submitRangeDelete() {
  if (!rangeDeleteForm.ticketId) {
    ElMessage({ type: 'warning', message: '请先选择具体票种' }); return
  }
  if (selectedDates.size === 0) {
    ElMessage({ type: 'warning', message: '请至少选择一个日期' }); return
  }
  if (rangeDeleteForm.confirmText !== '确认删除') {
    ElMessage({ type: 'warning', message: '请输入"确认删除"以启用按钮' }); return
  }
  saving.value = true
  try {
    const res = await batchDeleteInventory({
      ticketId: rangeDeleteForm.ticketId,
      dates: [...selectedDates].sort(),
      onlyUnsold: rangeDeleteForm.onlyUnsold,
    })
    ElMessage({ type: 'success', message: summarizeBatchResult(res) })
    showRangeDeleteModal.value = false
    rangeDeleteForm.confirmText = ''
    selectedDates.clear()
    loadInventories()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function closeInventory(item) {
  try {
    await toggleInventoryStatus(item.id, '关闭')
    ElMessage({ type: 'success', message: '已关闭' })
    loadInventories()
  } catch (e) { /* handled */ }
}

onMounted(async () => {
  // document 级 mouseup：保证鼠标在网格外松开也能结束拖选
  document.addEventListener('mouseup', endDragDate)
  await loadScenics()
  await loadTicketOptions()
  await loadInventories()
})

onBeforeUnmount(() => {
  document.removeEventListener('mouseup', endDragDate)
})
</script>

<style scoped>
/* 按日期删除弹窗：拖选日期网格 */
.date-btn {
  padding: 4px 8px;
  font-size: 12px;
  font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Consolas, monospace;
  background: #ffffff;
  color: var(--color-text-primary, #1f2937);
  border: 1px solid var(--color-border, #e5e7eb);
  border-radius: 3px;
  cursor: pointer;
  transition: background 0.08s, color 0.08s, border-color 0.08s;
  -webkit-user-select: none;
  user-select: none;
}
.date-btn:hover {
  background: var(--color-primary-light, #eef5ff);
  border-color: var(--color-primary, #3b82f6);
}
.date-btn.is-selected {
  background: var(--color-primary, #3b82f6);
  color: #ffffff;
  border-color: var(--color-primary, #3b82f6);
}
.date-btn.is-selected:hover {
  background: var(--color-primary, #2566c7);
  border-color: var(--color-primary, #2566c7);
}
</style>
