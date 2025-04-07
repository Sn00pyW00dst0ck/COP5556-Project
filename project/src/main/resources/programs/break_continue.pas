program BreakContinueTest;

var
  i: integer;

begin
  writeln('Testing FOR loop with continue and break:');

  for i := 1 to 10 do
  begin
    if i = 3 then
      continue;

    if i = 6 then
      break;

    writeln('i = ', i);
  end;

  writeln('Loop ended.');
end.