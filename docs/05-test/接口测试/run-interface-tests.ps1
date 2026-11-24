# 采购申请单接口测试执行脚本（PowerShell + curl.exe）
# 说明：Windows 下使用 curl.exe；含中文的 JSON 写成 UTF-8 无 BOM 文件后 --data-binary @file。
$ErrorActionPreference = 'Stop'
$baseUrl = 'http://127.0.0.1:8081'
$testDir = $PSScriptRoot
$respDir = Join-Path $testDir 'responses'
New-Item -ItemType Directory -Force $respDir | Out-Null
$runId = Get-Date -Format 'yyyyMMdd-HHmmss'
$results = @()

function Write-JsonFile([string]$path, [hashtable]$data) {
    $json = $data | ConvertTo-Json -Depth 10
    [IO.File]::WriteAllText($path, $json, [Text.UTF8Encoding]::new($false))
}

function Invoke-Case {
    param(
        [string]$CaseId,
        [string]$Name,
        [string]$Method,
        [string]$Path,
        [string]$BodyFile,
        [int]$ExpectedHttp,
        [scriptblock]$Validate
    )
    $url = "$baseUrl$Path"
    $bodyOut = Join-Path $respDir "$CaseId-$runId.json"
    $curlArgs = @('-s', '-m', '30', '-o', $bodyOut, '-w', '%{http_code}', '-X', $Method, $url,
        '-H', 'accept: application/json',
        "-H", "X-Request-Id: d7-$CaseId-$runId")
    if ($BodyFile) {
        $curlArgs += @('-H', 'Content-Type: application/json; charset=utf-8', '--data-binary', "@$BodyFile")
    }
    $http = & curl.exe @curlArgs
    $body = if (Test-Path $bodyOut) { Get-Content $bodyOut -Raw -Encoding utf8 } else { '' }
    $json = $null
    if ($body -and $body.Trim().StartsWith('{')) {
        try { $json = $body | ConvertFrom-Json } catch { $json = $null }
    }
    $pass = ($http -eq $ExpectedHttp)
    $detail = ''
    if ($pass -and $Validate) {
        $check = & $Validate $json $http
        if (-not $check.Pass) {
            $pass = $false
            $detail = $check.Detail
        }
    } elseif (-not $pass) {
        $detail = "HTTP=$http 期望=$ExpectedHttp"
    }
    $script:results += [pscustomobject]@{
        Case = $CaseId
        Name = $Name
        Method = $Method
        Path = $Path
        Http = $http
        ExpectedHttp = $ExpectedHttp
        Success = if ($json) { $json.success } else { $null }
        ErrorCode = if ($json) { $json.errorCode } else { $null }
        Msg = if ($json) { $json.msg } else { $null }
        Result = if ($pass) { 'PASS' } else { 'FAIL' }
        Detail = $detail
        Evidence = $bodyOut
    }
    Write-Host ("{0} {1} HTTP={2} {3} {4}" -f $CaseId, $Name, $http, ($json.success), ($(if ($json) { $json.errorCode } else { '' })))
}

function Check([bool]$pass, [string]$detail) {
    return [pscustomobject]@{ Pass = $pass; Detail = $detail }
}

function FieldEquals($obj, $field, $expected) {
    if ($null -eq $obj) { return $false }
    return [string]$obj.$field -eq [string]$expected
}

# ---------- 基础查询（种子数据） ----------
Invoke-Case -CaseId 'C01' -Name '分页查询默认' -Method GET -Path '/purchase-requests?current=1&pageSize=10' -ExpectedHttp 200 -Validate {
    param($j)
    Check ($j.success -eq $true -and $j.data.datas.Count -ge 1 -and [int]$j.data.total -ge 1) 'success/total/datas'
}
Invoke-Case -CaseId 'C02' -Name '按申请单号模糊查询' -Method GET -Path '/purchase-requests?current=1&pageSize=10&requestNoLike=SEED' -ExpectedHttp 200 -Validate {
    param($j)
    Check ($j.success -eq $true -and $j.data.datas.Count -ge 1 -and $j.data.datas[0].requestNo -like '*SEED*') 'requestNoLike 过滤'
}
Invoke-Case -CaseId 'C03' -Name '查询种子详情（主子表）' -Method GET -Path '/purchase-requests/880000000000000001' -ExpectedHttp 200 -Validate {
    param($j)
    Check ($j.success -eq $true -and $j.data.items.Count -eq 2 -and $j.data.status -eq 'SUBMITTED') 'items=2/status=SUBMITTED'
}

# ---------- 新增/修改/删除 ----------
$caseCreate = Join-Path $testDir 'case-create.json'
Write-JsonFile $caseCreate @{
    title = '测试采购申请A'; applicantName = '测试员甲'; department = '研发部'; remark = 'D7 接口测试'
    items = @(
        @{ itemName = '显示器'; spec = '27寸'; unit = '台'; quantity = 2; unitPrice = 99.90 },
        @{ itemName = '键盘'; spec = '机械'; unit = '个'; quantity = 1; unitPrice = 60.00 }
    )
}
Invoke-Case -CaseId 'C04' -Name '新增采购申请单（草稿）' -Method POST -Path '/purchase-requests' -BodyFile $caseCreate -ExpectedHttp 200 -Validate {
    param($j)
    Check ($j.success -eq $true -and ([string]$j.data) -match '^\d+$') 'success/id'
}
$createResp = Get-Content (Join-Path $respDir "C04-$runId.json") -Raw -Encoding utf8 | ConvertFrom-Json
$idA = [string]$createResp.data
Write-Host "idA=$idA"

Invoke-Case -CaseId 'C05' -Name '查询新建草稿详情' -Method GET -Path "/purchase-requests/$idA" -ExpectedHttp 200 -Validate {
    param($j)
    Check ($j.success -eq $true -and $j.data.status -eq 'DRAFT' -and $j.data.version -eq 0 -and [decimal]$j.data.totalAmount -eq 259.80 -and $j.data.items.Count -eq 2) 'draft/version=0/total=259.80'
}

$caseModify = Join-Path $testDir 'case-modify.json'
Write-JsonFile $caseModify @{
    title = '测试采购申请A-修改'; applicantName = '测试员甲'; department = '研发部'; remark = '修改后'
    version = 0
    items = @(@{ itemName = '鼠标'; unit = '个'; quantity = 3; unitPrice = 10.00 })
}
Invoke-Case -CaseId 'C06' -Name '修改草稿（版本0）' -Method PUT -Path "/purchase-requests/$idA" -BodyFile $caseModify -ExpectedHttp 200 -Validate {
    param($j)
    Check ($j.success -eq $true) 'success'
}
Invoke-Case -CaseId 'C07' -Name '修改后详情（版本递增、金额重算）' -Method GET -Path "/purchase-requests/$idA" -ExpectedHttp 200 -Validate {
    param($j)
    Check ($j.success -eq $true -and $j.data.version -eq 1 -and [decimal]$j.data.totalAmount -eq 30.00 -and $j.data.items.Count -eq 1 -and $j.data.title -eq '测试采购申请A-修改') 'version=1/total=30.00/items=1'
}
Invoke-Case -CaseId 'C08' -Name '重复使用旧版本修改（乐观锁冲突）' -Method PUT -Path "/purchase-requests/$idA" -BodyFile $caseModify -ExpectedHttp 409 -Validate {
    param($j)
    Check ($j.success -eq $false -and $j.errorCode -eq '90000206') '90000206 版本冲突'
}

# ---------- 提交/驳回/撤销/取消/审批 ----------
$caseVersion1 = Join-Path $testDir 'case-version-1.json'
Write-JsonFile $caseVersion1 @{ version = 1 }
Invoke-Case -CaseId 'C09' -Name '提交草稿' -Method POST -Path "/purchase-requests/$idA/submit" -BodyFile $caseVersion1 -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
Invoke-Case -CaseId 'C10' -Name '提交后详情' -Method GET -Path "/purchase-requests/$idA" -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true -and $j.data.status -eq 'SUBMITTED' -and $j.data.version -eq 2) 'SUBMITTED/version=2' }
Invoke-Case -CaseId 'C11' -Name '重复提交（非法状态流转）' -Method POST -Path "/purchase-requests/$idA/submit" -BodyFile (Join-Path $testDir 'case-version-2.json') -ExpectedHttp 409 -Validate { param($j) Check ($j.errorCode -eq '90000203') '90000203 状态不允许' }
Invoke-Case -CaseId 'C12' -Name '撤回（待审批 -> 草稿）' -Method POST -Path "/purchase-requests/$idA/withdraw" -BodyFile (Join-Path $testDir 'case-version-2.json') -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
$caseCancel = Join-Path $testDir 'case-cancel.json'
Write-JsonFile $caseCancel @{ version = 3; reason = '需求变更，取消申请' }
Invoke-Case -CaseId 'C13' -Name '取消（原因必填-带原因）' -Method POST -Path "/purchase-requests/$idA/cancel" -BodyFile $caseCancel -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
Invoke-Case -CaseId 'C14' -Name '取消后详情（原因/时间）' -Method GET -Path "/purchase-requests/$idA" -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true -and $j.data.status -eq 'CANCELLED' -and $j.data.cancelReason -eq '需求变更，取消申请' -and $j.data.cancelledAt) 'CANCELLED/reason/time' }
Invoke-Case -CaseId 'C15' -Name '已取消单据再次取消（非法状态）' -Method POST -Path "/purchase-requests/$idA/cancel" -BodyFile (Join-Path $testDir 'case-cancel.json') -ExpectedHttp 409 -Validate { param($j) Check ($j.errorCode -eq '90000203') '90000203' }

# 创建 B：提交 -> 审批通过
$caseCreateB = Join-Path $testDir 'case-create-b.json'
Write-JsonFile $caseCreateB @{ title = '测试采购申请B'; applicantName = '测试员乙'; department = '采购部'; items = @(@{ itemName = '打印机'; unit = '台'; quantity = 1; unitPrice = 1200.00 }) }
Invoke-Case -CaseId 'C16' -Name '新增申请B' -Method POST -Path '/purchase-requests' -BodyFile $caseCreateB -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
$idB = [string]((Get-Content (Join-Path $respDir "C16-$runId.json") -Raw -Encoding utf8 | ConvertFrom-Json).data)
Invoke-Case -CaseId 'C17' -Name '提交申请B' -Method POST -Path "/purchase-requests/$idB/submit" -BodyFile (Join-Path $testDir 'case-version-0.json') -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
$caseApprove = Join-Path $testDir 'case-approve.json'
Write-JsonFile $caseApprove @{ version = 1; approverName = '测试经理'; opinion = '同意采购' }
Invoke-Case -CaseId 'C18' -Name '审批通过申请B' -Method POST -Path "/purchase-requests/$idB/approve" -BodyFile $caseApprove -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
Invoke-Case -CaseId 'C19' -Name '审批通过后详情' -Method GET -Path "/purchase-requests/$idB" -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true -and $j.data.status -eq 'APPROVED' -and $j.data.approvalUserName -eq '测试经理' -and $j.data.approvalOpinion -eq '同意采购') 'APPROVED/审批信息' }
Invoke-Case -CaseId 'C20' -Name '已通过单据重复审批' -Method POST -Path "/purchase-requests/$idB/approve" -BodyFile (Join-Path $testDir 'case-version-2.json') -ExpectedHttp 409 -Validate { param($j) Check ($j.errorCode -eq '90000203') '90000203' }

# 创建 C：驳回（无原因/有原因）
$caseCreateC = Join-Path $testDir 'case-create-c.json'
Write-JsonFile $caseCreateC @{ title = '测试采购申请C'; applicantName = '测试员丙'; department = '财务部'; items = @(@{ itemName = '办公椅'; unit = '把'; quantity = 2; unitPrice = 350.00 }) }
Invoke-Case -CaseId 'C21' -Name '新增申请C' -Method POST -Path '/purchase-requests' -BodyFile $caseCreateC -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
$idC = [string]((Get-Content (Join-Path $respDir "C21-$runId.json") -Raw -Encoding utf8 | ConvertFrom-Json).data)
Invoke-Case -CaseId 'C22' -Name '提交申请C' -Method POST -Path "/purchase-requests/$idC/submit" -BodyFile (Join-Path $testDir 'case-version-0.json') -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
$caseRejectNoReason = Join-Path $testDir 'case-reject-no-reason.json'
Write-JsonFile $caseRejectNoReason @{ version = 1; reason = '' }
Invoke-Case -CaseId 'C23' -Name '驳回原因必填（空原因）' -Method POST -Path "/purchase-requests/$idC/reject" -BodyFile $caseRejectNoReason -ExpectedHttp 400 -Validate { param($j) Check ($j.success -eq $false -and $j.errorCode -eq '90000205') '90000205 原因必填' }
$caseReject = Join-Path $testDir 'case-reject.json'
Write-JsonFile $caseReject @{ version = 1; reason = '预算不足' }
Invoke-Case -CaseId 'C24' -Name '驳回申请C（带原因）' -Method POST -Path "/purchase-requests/$idC/reject" -BodyFile $caseReject -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
Invoke-Case -CaseId 'C25' -Name '驳回后详情（原因记录）' -Method GET -Path "/purchase-requests/$idC" -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true -and $j.data.status -eq 'REJECTED' -and $j.data.approvalOpinion -eq '预算不足') 'REJECTED/驳回原因' }

# 参数校验
$caseEmpty = Join-Path $testDir 'case-invalid-empty.json'
Write-JsonFile $caseEmpty @{ title = ''; applicantName = '测试员'; department = '研发部'; items = @() }
Invoke-Case -CaseId 'C26' -Name '新增参数校验（标题空/明细空）' -Method POST -Path '/purchase-requests' -BodyFile $caseEmpty -ExpectedHttp 400 -Validate { param($j) Check ($j.success -eq $false) '400 参数校验' }

# 删除
$caseCreateD = Join-Path $testDir 'case-create-d.json'
Write-JsonFile $caseCreateD @{ title = '测试采购申请D'; applicantName = '测试员丁'; department = '行政部'; items = @(@{ itemName = '文件柜'; unit = '个'; quantity = 1; unitPrice = 800.00 }) }
Invoke-Case -CaseId 'C27' -Name '新增申请D' -Method POST -Path '/purchase-requests' -BodyFile $caseCreateD -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
$idD = [string]((Get-Content (Join-Path $respDir "C27-$runId.json") -Raw -Encoding utf8 | ConvertFrom-Json).data)
Invoke-Case -CaseId 'C28' -Name '删除草稿申请D' -Method DELETE -Path "/purchase-requests/$idD" -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
Invoke-Case -CaseId 'C29' -Name '删除后详情（不存在）' -Method GET -Path "/purchase-requests/$idD" -ExpectedHttp 404 -Validate { param($j) Check ($j.success -eq $false -and $j.errorCode -eq '90000202') '90000202' }
Invoke-Case -CaseId 'C30' -Name '删除非草稿单据（已通过B）' -Method DELETE -Path "/purchase-requests/$idB" -ExpectedHttp 409 -Validate { param($j) Check ($j.errorCode -eq '90000203') '90000203' }

# 并发提交（同一版本两个请求）
$caseCreateE = Join-Path $testDir 'case-create-e.json'
Write-JsonFile $caseCreateE @{ title = '测试采购申请E'; applicantName = '测试员戊'; department = '研发部'; items = @(@{ itemName = '测试仪'; unit = '台'; quantity = 1; unitPrice = 500.00 }) }
Invoke-Case -CaseId 'C31' -Name '新增申请E' -Method POST -Path '/purchase-requests' -BodyFile $caseCreateE -ExpectedHttp 200 -Validate { param($j) Check ($j.success -eq $true) 'success' }
$idE = [string]((Get-Content (Join-Path $respDir "C31-$runId.json") -Raw -Encoding utf8 | ConvertFrom-Json).data)
$caseV0 = Join-Path $testDir 'case-version-0.json'
Write-JsonFile $caseV0 @{ version = 0 }
$job1 = Start-Job -ScriptBlock {
    param($u, $f)
    & curl.exe -s -m 30 -o "$env:TEMP\c32a.json" -w '%{http_code}' -X POST $u -H 'Content-Type: application/json' --data-binary "@$f"
} -ArgumentList "http://127.0.0.1:8081/purchase-requests/$idE/submit", $caseV0
$job2 = Start-Job -ScriptBlock {
    param($u, $f)
    & curl.exe -s -m 30 -o "$env:TEMP\c32b.json" -w '%{http_code}' -X POST $u -H 'Content-Type: application/json' --data-binary "@$f"
} -ArgumentList "http://127.0.0.1:8081/purchase-requests/$idE/submit", $caseV0
$h1 = Receive-Job $job1 -Wait; $h2 = Receive-Job $job2 -Wait
Remove-Job $job1, $job2 -Force
$codes = @($h1, $h2) | Sort-Object
$passConcurrent = (($codes -contains '200') -and ($codes -contains '409'))
$results += [pscustomobject]@{
    Case = 'C32'; Name = '并发提交同一版本（乐观锁）'; Method = 'POST'; Path = "/purchase-requests/$idE/submit"
    Http = ($codes -join '/'); ExpectedHttp = '200+409'; Success = $null; ErrorCode = $null; Msg = $null
    Result = $(if ($passConcurrent) { 'PASS' } else { 'FAIL' }); Detail = "HTTP组合=$($codes -join '/')"; Evidence = 'C32 并发响应见临时文件'
}
Write-Host ("C32 并发提交 HTTP组合={0} {1}" -f ($codes -join '/'), $(if ($passConcurrent) { 'PASS' } else { 'FAIL' }))

# 输出结果
$results | Export-Csv -Path (Join-Path $testDir "interface-test-results-$runId.csv") -NoTypeInformation -Encoding UTF8
$results | ConvertTo-Json -Depth 3 | Set-Content -Path (Join-Path $testDir "interface-test-results-$runId.json") -Encoding utf8
$passCount = ($results | Where-Object { $_.Result -eq 'PASS' }).Count
Write-Host "TOTAL=$($results.Count) PASS=$passCount FAIL=$($results.Count - $passCount)"
